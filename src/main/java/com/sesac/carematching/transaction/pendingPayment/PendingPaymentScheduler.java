package com.sesac.carematching.transaction.pendingPayment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Scheduled(fixedDelay = RETRY_INTERVAL_MILLIS)
    public void retryPendingPayments() {
        Instant expireLimit = Instant.now().minusSeconds(PAYMENT_EXPIRE_MINUTES * 60);
        int page = 0;
        Page<PendingPayment> pendings;
        do {
            pendings = pendingPaymentRepository.findByConfirmedFalseAndCreatedAtAfter(
                expireLimit,
                PageRequest.of(page, BATCH_SIZE)
            );
            if (pendings != null && !pendings.isEmpty()) {
                for (PendingPayment pending : pendings) {
                    pendingPaymentAsyncProcessor.retrySinglePendingPayment(pending);
                }
                page++;
            }
        } while (pendings != null && !pendings.isEmpty());
    }
}
