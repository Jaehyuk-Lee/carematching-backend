package com.sesac.carematching.transaction.pendingPayment;

import com.sesac.carematching.transaction.PaymentService;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
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
    private final PaymentService paymentService;

    @Transactional
    @Async("pendingPaymentRetryExecutor")
    public void retrySinglePendingPayment(PendingPayment pending) {
        try {
            TransactionDetailDTO transactionDetailDTO = paymentService.confirmPayment(pending.getOrderId(), pending.getPrice(), pending.getPgPaymentKey());
            boolean result = transactionDetailDTO.getStatus().equals("DONE");
            if (result) {
                pending.setConfirmed(true);
                pending.setFailReason(null);
                log.info("PendingPayment confirm 성공: orderId={}", pending.getOrderId());
            } else {
                pending.setFailReason("결제 상태가 DONE이 아님 (confirm 실패)");
            }
        } catch (Exception e) {
            pending.setFailReason(e.getMessage());
            log.warn("PendingPayment confirm 재시도 실패: orderId={}, reason={}", pending.getOrderId(), e.getMessage());
        }
        pendingPaymentRepository.save(pending);
    }
}
