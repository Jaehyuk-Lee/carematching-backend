package com.sesac.carematching.transaction;

import com.sesac.carematching.transaction.dto.TransactionConfirmDTO;
import com.sesac.carematching.transaction.exception.PaymentVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConfirmFacade {

    private final TransactionService transactionService;

    // 트랜잭션 어노테이션을 걸지 않아 순수하게 흐름만 제어합니다.
    public TransactionConfirmDTO confirmTransaction(TransactionConfirmDTO transactionConfirmDTO, Integer userId, String paymentKey) {
        try {
            // 내부적으로 트랜잭션이 시작되고 정상 완료 시 커밋됨
            return transactionService.confirmTransaction(transactionConfirmDTO, userId, paymentKey);
        } catch (PaymentVerificationException e) {
            // PG사 승인 실패 등 명확한 결제 검증 예외 발생 시 내부 트랜잭션은 롤백 완료됨
            log.warn("[결제 검증 실패] orderId: {}, 사유: {}", transactionConfirmDTO.getOrderId(), e.getMessage());
            transactionService.markAsFailed(transactionConfirmDTO.getOrderId());

            throw e; // 글로벌 예외 처리기에서 응답을 생성하도록 다시 던짐
        }
    }
}
