package com.sesac.carematching.transaction;

import com.sesac.carematching.transaction.dto.TransactionConfirmDTO;
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
        } catch (IllegalStateException e) {
            // 사전 검증 실패(이미 승인된 거래, PG 미선택 등)는 결제 시도 전이므로 FAILED 처리하지 않음
            throw e;
        } catch (Exception e) {
            // PG 승인 실패, 네트워크 오류 등 결제 진행 중 예외 발생 시 FAILED 기록
            log.warn("[결제 실패] orderId: {}, 사유: {}", transactionConfirmDTO.getOrderId(), e.getMessage());
            transactionService.markAsFailed(transactionConfirmDTO.getOrderId());
            throw e;
        }
    }
}
