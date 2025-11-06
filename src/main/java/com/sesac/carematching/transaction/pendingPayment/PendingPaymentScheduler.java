package com.sesac.carematching.transaction.pendingPayment;

import com.sesac.carematching.transaction.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

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
        long lastId = 0L; // 처리 중인 데이터의 마지막 ID를 추적

        // Pageable 객체는 '정렬 순서'를 지정하기 위해 사용 (페이지 번호는 항상 0)
        Pageable pageable = PageRequest.of(0, BATCH_SIZE, Sort.by(Sort.Direction.ASC, "id"));

        // 스레드풀 대기 큐 상태 확인
        while (pendingPaymentRetryExecutor.getThreadPoolExecutor().getQueue().remainingCapacity() < BATCH_SIZE) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }


            List<PendingPayment> pendings = pendingPaymentRepository.findByPaymentProviderAndConfirmedFalseAndCreatedAtAfterAndIdGreaterThan(
                PaymentProvider.TOSS, // PG사 이름
                expireLimit, // 시간이 만료된 결제는 처리하지 않음
                lastId,      // 마지막 처리한 ID
                pageable     // (page=0, size=200, sort=id,ASC)
            );
            if (pendings == null || pendings.isEmpty()) {
                break;
            }
            for (PendingPayment pending : pendings) {
                pendingPaymentAsyncProcessor.retrySinglePendingPayment(pending);
            }

            // 다음 조회를 위해 현재 배치의 가장 마지막 ID를 저장
            lastId = pendings.getLast().getId();
        }
    }
}
