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

@Slf4j
@Component
public class PendingPaymentAsyncProcessor {
    private final TransactionRepository transactionRepository;
    private final Map<PaymentProvider, PaymentService> paymentServices = new EnumMap<>(PaymentProvider.class);

    public PendingPaymentAsyncProcessor(TransactionRepository transactionRepository, PaymentService tossPaymentService, PaymentService kakaoPayService) {
        this.transactionRepository = transactionRepository;
        this.paymentServices.put(PaymentProvider.TOSS, tossPaymentService);
        this.paymentServices.put(PaymentProvider.KAKAO, kakaoPayService);
    }

    @Transactional
    @Async("pendingPaymentRetryExecutor")
    public void retrySinglePendingPayment(Transaction transaction) {
        // 이미 처리된 건이면 재시도하지 않음 (멱등성)
        if (transaction.getTransactionStatus() != TransactionStatus.PENDING_RETRY) {
            log.info("이미 처리된 트랜잭션입니다: orderId={}", transaction.getOrderId());
            return;
        }

        PendingPayment pendingPayment = transaction.getPendingPayment();
        if (pendingPayment == null) {
            log.error("재시도 대상 트랜잭션에 PendingPayment 정보가 없습니다. orderId: {}", transaction.getOrderId());
            transaction.changeTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return;
        }

        PaymentProvider nowPg = transaction.getPaymentProvider();
        PaymentConfirmRequestDTO request = new PaymentConfirmRequestDTO(
            transaction.getOrderId(),
            transaction.getPrice(),
            transaction.getPgPaymentKey(), // 초기 결제 시도 시 저장된 paymentKey 사용
            null,
            null
        );

        if (nowPg == PaymentProvider.KAKAO) {
            request.setPgToken(pendingPayment.getPgToken());
            request.setPartnerUserId(pendingPayment.getPartnerUserId());
        }

        try {
            PaymentService paymentService = this.paymentServices.get(nowPg);
            TransactionDetailDTO transactionDetailDTO = paymentService.confirmPayment(request);

            if (transactionDetailDTO.getPgStatus() == PgStatus.DONE) {
                transaction.changeTransactionStatus(TransactionStatus.SUCCESS);
                // PG사로부터 받은 최종 paymentKey로 업데이트
                transaction.setPgPaymentKey(transactionDetailDTO.getPaymentKey());
                pendingPayment.setFailReason(null);
                log.info("{} PendingPayment confirm 성공: orderId={}", nowPg, transaction.getOrderId());
            } else {
                transaction.changeTransactionStatus(TransactionStatus.FAILED);
                pendingPayment.setFailReason("결제 상태가 DONE이 아님 (confirm 실패): " + transactionDetailDTO.getPgStatus());
                log.warn("PendingPayment confirm 재시도 실패: orderId={}, pgStatus={}", transaction.getOrderId(), transactionDetailDTO.getPgStatus());
            }
        } catch (Exception e) {
            if (transaction.getTransactionStatus() != TransactionStatus.PENDING_RETRY) {
                log.warn("재시도 중 예외 발생했으나 트랜잭션 상태가 이미 변경됨. orderId={}, 현재 상태={}",
                    transaction.getOrderId(), transaction.getTransactionStatus());
                return;
            }
            transaction.changeTransactionStatus(TransactionStatus.FAILED);
            pendingPayment.setFailReason(e.getMessage());
            log.warn("PendingPayment confirm 재시도 중 예외 발생: orderId={}, reason={}", transaction.getOrderId(), e.getMessage());
        }

        // Transaction 저장 시 PendingPayment도 함께 저장됨 (Cascade)
        transactionRepository.save(transaction);
    }
}
