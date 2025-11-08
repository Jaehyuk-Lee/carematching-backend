package com.sesac.carematching.transaction.payment.pendingPayment;

import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.PaymentService;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.payment.PgStatus;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class PendingPaymentAsyncProcessor {
    private final PendingPaymentRepository pendingPaymentRepository;
    private final Map<PaymentProvider, PaymentService> paymentServices = new EnumMap<>(PaymentProvider.class);

    public PendingPaymentAsyncProcessor(PendingPaymentRepository pendingPaymentRepository, PaymentService tossPaymentService, PaymentService kakaoPayService) {
        this.pendingPaymentRepository = pendingPaymentRepository;
        this.paymentServices.put(PaymentProvider.TOSS, tossPaymentService);
        this.paymentServices.put(PaymentProvider.KAKAO, kakaoPayService);
    }

    @Transactional
    @Async("pendingPaymentRetryExecutor")
    public void retrySinglePendingPayment(PendingPayment pending) {
        PaymentConfirmRequestDTO request = PaymentConfirmRequestDTO.builder()
            .orderId(pending.getOrderId())
            .amount(pending.getPrice())
            .paymentKey(pending.getPgPaymentKey())
            .build();
        PaymentProvider nowPg = pending.getPaymentProvider();
        try {
            PaymentService paymentService = this.paymentServices.get(nowPg);
            TransactionDetailDTO transactionDetailDTO = paymentService.confirmPayment(request);

            if (transactionDetailDTO.getPgStatus() == PgStatus.DONE) {
                pending.setConfirmed(true);
                pending.setFailReason(null);
                log.info("{} PendingPayment confirm 성공: orderId={}", nowPg, pending.getOrderId());
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
