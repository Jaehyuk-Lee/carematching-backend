package com.sesac.carematching.tools;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;

public class LoadTestNoPool {
    public static void main(String[] args) throws Exception {
        String host = args.length>0 ? args[0] : "127.0.0.1";
        int port = args.length>1 ? Integer.parseInt(args[1]) : 6379;
        int threads = args.length>2 ? Integer.parseInt(args[2]) : 100;
        int opsPerIteration = args.length>3 ? Integer.parseInt(args[3]) : 100; // per loop iteration
        long durationSeconds = args.length>4 ? Long.parseLong(args[4]) : -1L; // -1 => run until interrupted

        ExecutorService ex = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger errors = new AtomicInteger();
        // cumulative latency over entire run
        LongAdder cumulativeLatencyNs = new LongAdder();
        // interval latency which is reset every stats interval
        LongAdder intervalLatencyNs = new LongAdder();
        // samples for the current interval (synchronized list cleared each interval)
        List<Long> samples = Collections.synchronizedList(new ArrayList<>());
        // cumulative samples for final percentile calculation
        ConcurrentLinkedQueue<Long> cumulativeSamples = new ConcurrentLinkedQueue<>();

        AtomicBoolean running = new AtomicBoolean(true);
        long globalStart = System.nanoTime();
        if (durationSeconds > 0) {
            ScheduledExecutorService stopper = Executors.newSingleThreadScheduledExecutor();
            stopper.schedule(() -> running.set(false), durationSeconds, TimeUnit.SECONDS);
            stopper.shutdown();
        }

        // worker threads: each thread loops until running==false
        for (int t = 0; t < threads; t++) {
            ex.submit(() -> {
                try {
                    while (running.get()) {
                        for (int i = 0; i < opsPerIteration && running.get(); i++) {
                            String uri = "redis://" + host + ":" + port;
                            RedisClient client = null;
                            long s = System.nanoTime();
                            try {
                                client = RedisClient.create(uri);
                                StatefulRedisConnection<String,String> conn = client.connect();
                                RedisCommands<String, String> cmds = conn.sync();
                                String key = "loadtest:" + Thread.currentThread().getId() + ":" + i;
                                cmds.set(key, "v");
                                cmds.get(key);
                                conn.close();
                            } catch (Exception e) {
                                errors.incrementAndGet();
                            } finally {
                                long elapsed = System.nanoTime() - s;
                                // record both cumulative and interval metrics
                                cumulativeLatencyNs.add(elapsed);
                                intervalLatencyNs.add(elapsed);
                                cumulativeSamples.add(elapsed);
                                samples.add(elapsed);
                                if (client != null) {
                                    try { client.shutdown(); } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 주기적 통계 로그 (5초) — 출력 후 해당 집계는 비워서 메모리 누적을 방지
        ScheduledExecutorService statsEx = Executors.newSingleThreadScheduledExecutor();
        statsEx.scheduleAtFixedRate(() -> {
            // capture and reset accumulated latency for this interval so avg matches interval samples
            long intervalSum = intervalLatencyNs.sumThenReset();
            List<Long> drained;
            synchronized (samples) {
                drained = new ArrayList<>(samples);
                samples.clear();
            }
            printStats("NoPool-interval", drained, intervalSum, errors.get());
        }, 5, 5, TimeUnit.SECONDS);

        // wait until stopped
        latch.await();
        statsEx.shutdownNow();
        long durMs = (System.nanoTime() - globalStart) / 1_000_000;
        ex.shutdownNow();
        // prepare final cumulative stats
        long finalSum = cumulativeLatencyNs.sum();
        List<Long> finalDrained = new ArrayList<>();
        Long v;
        while ((v = cumulativeSamples.poll()) != null) finalDrained.add(v);
        printStats("NoPool-Final", finalDrained, finalSum, errors.get());
        System.out.printf("NoPool finished: durationMs=%d errors=%d totalSamples=%d%n",
            durMs, errors.get(), finalDrained.size());
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
