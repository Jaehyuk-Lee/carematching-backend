package com.sesac.carematching.transaction.pendingPayment;

import com.sesac.carematching.transaction.TossPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingPaymentAsyncProcessor {
    private final PendingPaymentRepository pendingPaymentRepository;
    private final TossPaymentService tossPaymentService;

    @Transactional
    @Async("pendingPaymentRetryExecutor")
    public void retrySinglePendingPayment(PendingPayment pending) {
        try {
            boolean result = tossPaymentService.verifyTossPayment(pending.getOrderId(), pending.getPrice(), pending.getPaymentKey());
            if (result) {
                pending.setConfirmed(true);
                pending.setFailReason(null);
                log.info("PendingPayment confirm 성공: orderId={}", pending.getOrderId());
            } else {
                pending.setFailReason("결제 상태가 DONE이 아님");
            }
        } catch (Exception e) {
            pending.setFailReason(e.getMessage());
            log.warn("PendingPayment confirm 재시도 실패: orderId={}, reason={}", pending.getOrderId(), e.getMessage());
        }
        pendingPaymentRepository.save(pending);
    }
}
