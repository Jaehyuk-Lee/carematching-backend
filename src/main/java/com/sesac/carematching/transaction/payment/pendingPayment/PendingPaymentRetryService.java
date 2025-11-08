package com.sesac.carematching.transaction.payment.pendingPayment;

import com.sesac.carematching.transaction.payment.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Provider별로 페이먼트 재시도 작업을 실행하는 서비스.
 * 스케줄러나 이벤트 리스너에서 호출하면 해당 Provider에 대한 PendingPayment만 재시도합니다.
 */
@Service
@RequiredArgsConstructor
public class PendingPaymentRetryService {
    // 결제 만료 시간 (10분)
    private final static long PAYMENT_EXPIRE_MINUTES = 10;
    // 배치 사이즈
    private static final int BATCH_SIZE = 200;

    private final PendingPaymentRepository pendingPaymentRepository;
    private final PendingPaymentAsyncProcessor pendingPaymentAsyncProcessor;
    private final ThreadPoolTaskExecutor pendingPaymentRetryExecutor;

    public void retryPendingPaymentsForProvider(PaymentProvider paymentProvider) {
        Instant expireLimit = Instant.now().minusSeconds(PAYMENT_EXPIRE_MINUTES * 60);
        // 상태 변화에 따른 페이징 깨짐 방지 (Executor가 엔티티의 상태를 변화시킴)
        // lastId로 처리 상태 저장 + Pageable로 정렬
        long lastId = 0L; // 처리 중인 데이터의 마지막 ID를 추적

        // Pageable 객체는 '정렬 순서'를 지정하기 위해 사용 (페이지 번호는 항상 0)
        Pageable pageable = PageRequest.of(0, BATCH_SIZE, Sort.by(Sort.Direction.ASC, "id"));

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

            List<PendingPayment> pendings = pendingPaymentRepository.findByPaymentProviderAndConfirmedFalseAndCreatedAtAfterAndIdGreaterThan(
                paymentProvider, // PG사 이름
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
