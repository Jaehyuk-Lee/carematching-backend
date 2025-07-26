package com.sesac.carematching.transaction.pendingPayment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PendingPaymentScheduler {
    // 자동 재시도 주기 (1분)
    private final static long RETRY_INTERVAL_MILLIS = 60_000L;
    // 결제 만료 시간 (10분)
    private final static long PAYMENT_EXPIRE_MINUTES = 10;
    // 배치 사이즈
    private static final int BATCH_SIZE = 200;

    private final PendingPaymentRepository pendingPaymentRepository;
    private final PendingPaymentAsyncProcessor pendingPaymentAsyncProcessor;
    private final ThreadPoolTaskExecutor pendingPaymentRetryExecutor;

    @Scheduled(fixedDelay = RETRY_INTERVAL_MILLIS)
    public void retryPendingPayments() {
        Instant expireLimit = Instant.now().minusSeconds(PAYMENT_EXPIRE_MINUTES * 60);
        int page = 0;
        while (true) {
            // 스레드풀 대기 큐 상태 확인
            int queueLeft = pendingPaymentRetryExecutor.getThreadPoolExecutor().getQueue().remainingCapacity();
            if (queueLeft < BATCH_SIZE) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            Page<PendingPayment> pendings = pendingPaymentRepository.findByConfirmedFalseAndCreatedAtAfter(
                expireLimit,
                PageRequest.of(page, BATCH_SIZE)
            );
            if (pendings == null || pendings.isEmpty()) {
                break;
            }
            for (PendingPayment pending : pendings) {
                pendingPaymentAsyncProcessor.retrySinglePendingPayment(pending);
            }
            page++;
        }
    }
}
