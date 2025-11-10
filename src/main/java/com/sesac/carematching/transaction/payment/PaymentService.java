package com.sesac.carematching.transaction.payment;

import com.sesac.carematching.transaction.dto.PaymentReadyRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyResponseDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;

/**
 * 결제 제공자(예: TossPayments, 다른 PG)들을 추상화한 서비스 인터페이스.
 */
public interface PaymentService {
    /**
     * 외부 PG에 결제 준비를 요청합니다.
     *
     * @param request 결제 준비 정보
     * @return 결제 준비 응답 (리다이렉트 URL 등)
     */
    PaymentReadyResponseDTO readyPayment(PaymentReadyRequestDTO request);

    /**
     * 외부 PG에 결제 검증을 요청합니다.
     *
     * @param request   주문 승인 정보
     * @return 결제 상세 정보 (status가 DONE이면 승인 완료)
     */
    TransactionDetailDTO confirmPayment(PaymentConfirmRequestDTO request);
}
