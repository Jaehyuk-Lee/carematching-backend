package com.sesac.carematching.transaction.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.transaction.Transaction;
import com.sesac.carematching.transaction.TransactionRepository;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyResponseDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPayment;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPaymentRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPaymentService implements PaymentService{
    protected final TransactionRepository transactionRepository;
    protected final PendingPaymentRecorder pendingPaymentRecorder;

    protected <T extends RuntimeException> T parsePaymentError(String errorJson, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(errorJson, valueType);
        } catch (JsonProcessingException e) {
            log.warn("{} 파싱 실패: {}", valueType.getSimpleName(), errorJson, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 결제 준비 단계가 필요 없는 PG사를 위한 기본 구현입니다.
     * 결제 준비가 필요한 PG사(예: KakaoPay)에서는 이 메서드를 오버라이드해야 합니다.
     */
    @Override
    public PaymentReadyResponseDTO readyPayment(PaymentReadyRequestDTO request) {
        return new PaymentReadyResponseDTO();
    }

    /**
     * 결제 준비 헬스체크가 필요 없는 PG사를 위한 기본 구현입니다.
     * 결제 준비 헬스체크가 필요한 PG사(예: KakaoPay)에서는 이 메서드를 오버라이드해야 합니다.
     */
    @Override
    public void healthCheckReady(PaymentReadyRequestDTO request) {
        // no-op
    }

    /**
     * PendingPayment 재시도 시 공통 필드(orderId, amount, paymentKey)만으로 요청을 구성합니다.
     * PG사별 추가 필드가 필요한 경우(예: KakaoPay의 pgToken) 오버라이드하세요.
     */
    @Override
    public PaymentConfirmRequestDTO buildRetryConfirmRequest(Transaction transaction) {
        return PaymentConfirmRequestDTO.builder()
            .orderId(transaction.getOrderId())
            .amount(transaction.getPrice())
            .paymentKey(transaction.getPgPaymentKey())
            .build();
    }

    /**
     * confirmPayment 서킷브레이커 OPEN시 실행할 공통 fallback 메서드
     * 기본적으로 Transaction 엔티티의 TransactionStatus를 PENDING_RETRY로 변경
     * 각 PG사에 알맞게 PendingPayment에 추가 저장 가능 (필요시 customizePendingPayment 구현)
     *
     * <p>DB 기록은 {@link PendingPaymentRecorder}에 위임한다. 이 fallback은 외부 결제 호출을 감싸는
     * 상위 트랜잭션 안에서 실행되고, 직후 {@code FallbackCheckAspect}가 던지는 예외로 상위 트랜잭션이
     * 롤백되기 때문이다. recorder는 REQUIRES_NEW로 별도 커밋하여 재시도 예약이 롤백되지 않도록 한다.
     * (resilience4j는 fallback을 프록시가 아닌 raw target에 직접 호출하므로 이 메서드에
     * {@code @Transactional}을 붙여도 동작하지 않는다 — 반드시 별도 빈으로 분리해야 한다.)</p>
     */
    protected TransactionDetailDTO fallbackForConfirm(PaymentConfirmRequestDTO request, Throwable t) {
        PaymentProvider provider = getPaymentProvider();

        // 각 PG사에 알맞게 PendingPayment에 추가 저장 (필요시 customizePendingPayment 구현)
        pendingPaymentRecorder.record(
            request.getOrderId(),
            provider,
            request.getPaymentKey(),
            pending -> customizePendingPayment(pending, request));

        log.warn("{} confirm fallback: 결제 재시도 상태로 전환. orderId={}",
            provider, request.getOrderId());

        TransactionDetailDTO transactionDetailDTO = new TransactionDetailDTO();
        transactionDetailDTO.setFallback(true);
        return transactionDetailDTO;
    }

    /**
     * [HOOK METHOD]
     * 자식 클래스에서 PendingPayment 저장이 필요한 추가 정보를 설정할 수 있도록 오버라이딩을 허용합니다.
     * 기본적으로는 아무 작업도 수행하지 않습니다.
     *
     * @param pending 저장될 PendingPayment 엔티티
     * @param request 원본 결제 승인 요청 DTO
     */
    protected void customizePendingPayment(PendingPayment pending, PaymentConfirmRequestDTO request) {
        // 필요시 자식 클래스에서 구현
    }
}
