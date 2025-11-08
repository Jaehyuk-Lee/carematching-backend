package com.sesac.carematching.transaction.payment.pendingPayment;

import com.sesac.carematching.transaction.Transaction;
import com.sesac.carematching.transaction.TransactionRepository;
import com.sesac.carematching.transaction.TransactionStatus;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.PaymentService;
import com.sesac.carematching.transaction.payment.PgStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class PendingPaymentAsyncProcessor {
    private final TransactionRepository transactionRepository;
    private final PendingPaymentRepository pendingPaymentRepository;
    private final Map<PaymentProvider, PaymentService> paymentServices = new EnumMap<>(PaymentProvider.class);

    public PendingPaymentAsyncProcessor(TransactionRepository transactionRepository, PendingPaymentRepository pendingPaymentRepository, PaymentService tossPaymentService, PaymentService kakaoPayService) {
        this.transactionRepository = transactionRepository;
        this.pendingPaymentRepository = pendingPaymentRepository;
        this.paymentServices.put(PaymentProvider.TOSS, tossPaymentService);
        this.paymentServices.put(PaymentProvider.KAKAO, kakaoPayService);
    }

    @Transactional
    @Async("pendingPaymentRetryExecutor")
    public void retrySinglePendingPayment(PendingPayment pending) {
        // Transaction 존재 확인
        Optional<Transaction> transactionOpt = transactionRepository.findByOrderId(pending.getOrderId());
        if (transactionOpt.isEmpty()) {
            log.warn("Transaction 테이블에서 제거된 PendingPayment는 재시도하지 않고 제거합니다. PendingPayment: {}", pending);
            pendingPaymentRepository.delete(pending);
            return;
        }

        Transaction transaction = transactionOpt.get();

        PaymentConfirmRequestDTO request = PaymentConfirmRequestDTO.builder()
            .orderId(pending.getOrderId())
            .amount(pending.getPrice())
            .paymentKey(pending.getPgPaymentKey())
            .build();
        PaymentProvider nowPg = pending.getPaymentProvider();
        if (nowPg == PaymentProvider.KAKAO) {
            request.setPgToken(pending.getPgToken());
            request.setPartnerUserId(pending.getPartnerUserId());
        }
        try {
            PaymentService paymentService = this.paymentServices.get(nowPg);
            TransactionDetailDTO transactionDetailDTO = paymentService.confirmPayment(request);

            if (transactionDetailDTO.getPgStatus() == PgStatus.DONE) {
                pending.setConfirmed(true);
                pending.setFailReason(null);
                transaction.setTransactionStatus(TransactionStatus.SUCCESS);
                transactionRepository.save(transaction);
                log.info("{} PendingPayment confirm 성공: orderId={}", nowPg, pending.getOrderId());
            } else {
                pending.setFailReason("PendingPayment confirm 재시도 실패: 결제 상태 " + transactionDetailDTO.getPgStatus());
            }
        } catch (Exception e) {
            pending.setFailReason(e.getMessage());
            log.warn("PendingPayment confirm 재시도 실패: orderId={}, reason={}", pending.getOrderId(), e.getMessage());
        }
        pendingPaymentRepository.save(pending);
    }
}
