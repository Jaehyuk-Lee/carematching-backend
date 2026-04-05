package com.sesac.carematching.transaction.payment;

import com.sesac.carematching.transaction.Transaction;
import com.sesac.carematching.transaction.dto.PaymentReadyRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyResponseDTO;
import com.sesac.carematching.transaction.dto.TransactionConfirmDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;

/**
 * 결제 제공자(예: TossPayments, 다른 PG)들을 추상화한 서비스 인터페이스.
 */
public interface PaymentService {

    /**
     * 현재 클래스의 PaymentProvider 정보를 제공합니다.
     *
     * @return PaymentProvider 현재 클래스가 구현한 PG사 enum
     */
    PaymentProvider getPaymentProvider();

    /**
     * 외부 PG에 결제 준비를 요청합니다.
     *
     * @param request 결제 준비 정보
     * @return 결제 준비 응답 (리다이렉트 URL 등)
     */
    PaymentReadyResponseDTO readyPayment(PaymentReadyRequestDTO request);

    /**
     * 외부 PG사 결제 준비 엔드포인트에 대해 헬스체크를 수행합니다.
     *
     * @param request 결제 준비 정보
     */
    void healthCheckReady(PaymentReadyRequestDTO request);

    /**
     * 외부 PG에 결제 검증을 요청합니다.
     *
     * @param request   주문 승인 정보
     * @return 결제 상세 정보 (status가 DONE이면 승인 완료)
     */
    TransactionDetailDTO confirmPayment(PaymentConfirmRequestDTO request);

    /**
     * 외부 PG사 결제 검증 엔드포인트에 대해 헬스체크를 수행합니다.
     *
     * @param request 주문 승인 정보
     */
    void healthCheckConfirm(PaymentConfirmRequestDTO request);

    /**
     * PG사에 맞는 결제 승인 요청 DTO를 구성합니다.
     * 각 PG사마다 필요한 필드가 다르므로, 구현체가 직접 요청을 구성합니다.
     *
     * @param transaction  결제 대상 트랜잭션 엔티티
     * @param clientInput  클라이언트(프론트엔드)에서 전달된 요청 데이터
     * @param paymentKey   PG사에서 발급한 결제 키
     * @return 각 PG사에 맞게 구성된 PaymentConfirmRequestDTO
     */
    PaymentConfirmRequestDTO buildConfirmRequest(Transaction transaction, TransactionConfirmDTO clientInput, String paymentKey);

    /**
     * PendingPayment 재시도 시 사용할 결제 승인 요청 DTO를 구성합니다.
     *
     * @param transaction  재시도 대상 트랜잭션 엔티티 (PendingPayment 포함)
     * @return 재시도용 PaymentConfirmRequestDTO
     */
    PaymentConfirmRequestDTO buildRetryConfirmRequest(Transaction transaction);

}
