package com.sesac.carematching.transaction.tosspayments.pendingPayment;

import com.sesac.carematching.transaction.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Provider별로 페이먼트 재시도 작업을 실행하는 서비스.
 * 스케줄러나 이벤트 리스너에서 호출하면 해당 Provider에 대한 PendingPayment만 재시도합니다.
 */
@Service
@RequiredArgsConstructor
public class PendingPaymentRetryService {
    private static final int BATCH_SIZE = 200;

    private final PendingPaymentRepository pendingPaymentRepository;
    private final PendingPaymentAsyncProcessor pendingPaymentAsyncProcessor;

    public void retryPendingPaymentsForProvider(PaymentProvider provider) {
        Instant expireLimit = Instant.now().minusSeconds(10 * 60);
        int page = 0;
        while (true) {
            Page<PendingPayment> pendings = pendingPaymentRepository.findByPaymentProviderAndConfirmedFalseAndCreatedAtAfter(
                provider,
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
