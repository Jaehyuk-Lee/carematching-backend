package com.sesac.carematching.transaction;

import com.sesac.carematching.transaction.dto.TransactionDetailDTO;

/**
 * 결제 제공자(예: TossPayments, 다른 PG)들을 추상화한 서비스 인터페이스.
 */
public interface PaymentService {
    /**
     * 외부 PG에 결제 검증을 요청합니다.
     *
     * @param orderId   주문 ID
     * @param price     결제 금액
     * @param paymentKey PG에서 발급한 결제 키
     * @return 결제 상세 정보 (status가 DONE이면 승인 완료)
     */
    TransactionDetailDTO confirmPayment(String orderId, Integer price, String paymentKey);
}
