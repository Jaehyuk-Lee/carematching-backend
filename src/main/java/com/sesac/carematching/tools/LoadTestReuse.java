package com.sesac.carematching.tools;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;

public class LoadTestReuse {
    public static void main(String[] args) throws Exception {
        String host = args.length>0 ? args[0] : "127.0.0.1";
        int port = args.length>1 ? Integer.parseInt(args[1]) : 6379;
        int threads = args.length>2 ? Integer.parseInt(args[2]) : 100;
        int opsPerIteration = args.length>3 ? Integer.parseInt(args[3]) : 100;
        long durationSeconds = args.length>4 ? Long.parseLong(args[4]) : -1L;
        int targetQps = args.length>5 ? Integer.parseInt(args[5]) : 0; // target QPS across all threads (0 == unlimited)
        int poolSize = args.length>6 ? Integer.parseInt(args[6]) : 0; // 0 means no pool (reuse single connection)

        String uri = "redis://"+host+":"+port;
        RedisClient client = RedisClient.create(uri);

        // If poolSize > 0, create a commons-pool2-backed pool of StatefulRedisConnection
        GenericObjectPool<StatefulRedisConnection<String,String>> pool = null;
        StatefulRedisConnection<String,String> singleConn = null;
        RedisCommands<String,String> singleCmds = null;
        if (poolSize > 0) {
            GenericObjectPoolConfig<StatefulRedisConnection<String,String>> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(poolSize);
            poolConfig.setMaxIdle(Math.max(1, Math.min(poolSize, 10)));
            poolConfig.setMinIdle(1);
            poolConfig.setBlockWhenExhausted(true);
            pool = ConnectionPoolSupport.createGenericObjectPool(() -> client.connect(), poolConfig);
            System.out.printf("Using connection pool: maxTotal=%d\n", poolSize);
        } else {
            singleConn = client.connect();
            singleCmds = singleConn.sync();
            System.out.println("Using single shared connection (no pool)");
        }

        ExecutorService ex = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger errors = new AtomicInteger();
        // cumulative latency over entire run
        LongAdder cumulativeLatencyNs = new LongAdder();
        // interval latency which is reset every stats interval
        LongAdder intervalLatencyNs = new LongAdder();
        // samples for the current interval (drained each interval)
        ConcurrentLinkedQueue<Long> intervalSamples = new ConcurrentLinkedQueue<>();
        // cumulative samples for final percentile calculation
        ConcurrentLinkedQueue<Long> cumulativeSamples = new ConcurrentLinkedQueue<>();

        AtomicBoolean running = new AtomicBoolean(true);
        if (durationSeconds > 0) {
            ScheduledExecutorService stopper = Executors.newSingleThreadScheduledExecutor();
            stopper.schedule(() -> running.set(false), durationSeconds, TimeUnit.SECONDS);
            stopper.shutdown();
        }

        // Simple token-bucket rate limiter (refill every 100ms)
        final AtomicInteger tokens = new AtomicInteger(0);
        ScheduledExecutorService refillEx = null;
        if (targetQps > 0) {
            refillEx = Executors.newSingleThreadScheduledExecutor();
            final int refillIntervalMs = 100;
            final int perRefill = Math.max(1, targetQps * refillIntervalMs / 1000);
            tokens.set(0);
            refillEx.scheduleAtFixedRate(() -> {
                int cur;
                int cap = targetQps;
                do {
                    cur = tokens.get();
                    int next = Math.min(cap, cur + perRefill);
                    if (tokens.compareAndSet(cur, next)) break;
                } while (true);
            }, 0, refillIntervalMs, TimeUnit.MILLISECONDS);
            System.out.printf("Target QPS=%d enabled (refill every %dms, +%d tokens)\n", targetQps, refillIntervalMs, perRefill);
        }

        final GenericObjectPool<StatefulRedisConnection<String,String>> poolRef = pool;
        final RedisCommands<String,String> singleCmdsRef = singleCmds;
        final int targetQpsRef = targetQps;
        final AtomicInteger tokensRef = tokens;

        for (int t=0;t<threads;t++) {
            ex.submit(() -> {
                StatefulRedisConnection<String,String> threadConn = null;
                RedisCommands<String,String> threadCmds = null;
                try {
                    // borrow one connection per worker when using pool to avoid per-op borrow overhead
                    if (poolRef != null) {
                        try {
                            threadConn = poolRef.borrowObject();
                            threadCmds = threadConn.sync();
                        } catch (Exception e) {
                            // if borrow fails, fall back to single shared commands if available
                            threadConn = null;
                            threadCmds = null;
                        }
                    }

                    while (running.get()) {
                        for (int i=0;i<opsPerIteration && running.get();i++) {
                            // acquire token if rate limiting enabled (before issuing Redis calls)
                            if (targetQpsRef > 0) {
                                while (running.get()) {
                                    int cur = tokensRef.get();
                                    if (cur > 0 && tokensRef.compareAndSet(cur, cur - 1)) break;
                                    LockSupport.parkNanos(200_000);
                                }
                            }

                            long s = System.nanoTime();
                            try {
                                String key = "loadtest:" + Thread.currentThread().getId() + ":" + i;
                                if (threadCmds != null) {
                                    threadCmds.set(key, "v");
                                    threadCmds.get(key);
                                } else {
                                    singleCmdsRef.set(key, "v");
                                    singleCmdsRef.get(key);
                                }
                            } catch (Exception e) {
                                errors.incrementAndGet();
                            } finally {
                                long elapsed = System.nanoTime() - s;
                                // record both cumulative and interval metrics
                                cumulativeLatencyNs.add(elapsed);
                                intervalLatencyNs.add(elapsed);
                                cumulativeSamples.add(elapsed);
                                intervalSamples.add(elapsed);
                            }
                        }
                    }
                } finally {
                    try {
                        if (threadConn != null) poolRef.returnObject(threadConn);
                    } catch (Exception ignore) {}
                    latch.countDown();
                }
            });
        }

        ScheduledExecutorService statsEx = Executors.newSingleThreadScheduledExecutor();
        statsEx.scheduleAtFixedRate(() -> {
            // capture and reset accumulated latency for this interval so avg matches interval samples
            long intervalSum = intervalLatencyNs.sumThenReset();
            // drain intervalSamples into list for sorting/percentiles without blocking producers
            List<Long> drained = new ArrayList<>();
            Long v;
            while ((v = intervalSamples.poll()) != null) {
                drained.add(v);
            }
            printStats("Reuse-interval", drained, intervalSum, errors.get());
        }, 5, 5, TimeUnit.SECONDS);

        latch.await();
        statsEx.shutdownNow();
        ex.shutdownNow();
        // cleanup
        if (refillEx != null) refillEx.shutdownNow();
        if (singleConn != null) singleConn.close();
        if (pool != null) pool.close();
        client.shutdown();
        long finalSum = cumulativeLatencyNs.sum();
        List<Long> finalDrained = new ArrayList<>();
        Long v2;
        while ((v2 = cumulativeSamples.poll()) != null) finalDrained.add(v2);
        printStats("Reuse-Final", finalDrained, finalSum, errors.get());
            System.out.printf("Reuse finished: errors=%d totalLatencyNs=%d totalSamples=%d%n",
                errors.get(), finalSum, finalDrained.size());
    }

    private static void printStats(String tag, List<Long> samples, long totalLatencyNs, int errors) {
        int count = samples.size();
        if (count == 0) {
            System.out.printf("%s stats: count=0 errors=%d\n", tag, errors);
            return;
        }
        List<Long> copy;
        synchronized (samples) { copy = new ArrayList<>(samples); }
        Collections.sort(copy);
        long min = copy.get(0);
        long max = copy.get(copy.size()-1);
        double avgMs = (totalLatencyNs / (double)count) / 1_000_000.0;
        double p50 = copy.get((int)(0.50 * (copy.size()-1))) / 1_000_000.0;
        double p95 = copy.get((int)(0.95 * (copy.size()-1))) / 1_000_000.0;
        double p99 = copy.get((int)(0.99 * (copy.size()-1))) / 1_000_000.0;
        System.out.printf("%s stats: count=%d errors=%d avg=%.3fms min=%.3fms max=%.3fms p50=%.3fms p95=%.3fms p99=%.3fms\n",
            tag, count, errors, avgMs, min/1_000_000.0, max/1_000_000.0, p50, p95, p99);
    }
}
