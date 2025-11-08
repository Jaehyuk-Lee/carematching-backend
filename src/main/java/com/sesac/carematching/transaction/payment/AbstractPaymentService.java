package com.sesac.carematching.transaction.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPayment;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPaymentService implements PaymentService{
    private final PendingPaymentRepository pendingPaymentRepository;

    @Override
    public abstract TransactionDetailDTO confirmPayment(PaymentConfirmRequestDTO request);

    protected <T extends RuntimeException> T parsePaymentError(String errorJson, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(errorJson, valueType);
        } catch (JsonProcessingException e) {
            log.warn("{} 파싱 실패: {}", valueType.getSimpleName(), errorJson, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(valueType.getSimpleName() + " 파싱 실패: " + errorJson);
        }
    }

    /**
     * 현재 클래스의 PaymentProvider 정보를 제공합니다.
     * 자식 클래스에서 필수적으로 구현해야 합니다.
     *
     * @return PaymentProvider 현재 클래스가 구현한 PG사 enum
     */
    protected abstract PaymentProvider getPaymentProvider();

    /**
     * confirmPayment 서킷브레이커 OPEN시 실행할 공통 fallback 메서드
     * 각 PG사에 알맞게 PendingPayment에 추가 저장 가능 (필요시 customizePendingPayment 구현)
     */
    protected TransactionDetailDTO fallbackForConfirm(PaymentConfirmRequestDTO paymentConfirmRequestDTO, Throwable t) {
        PaymentProvider provider = getPaymentProvider();

        PendingPayment pending = new PendingPayment(
            paymentConfirmRequestDTO.getOrderId(),
            paymentConfirmRequestDTO.getPaymentKey(),
            paymentConfirmRequestDTO.getAmount(),
            provider
        );

        // 각 PG사에 알맞게 PendingPayment에 추가 저장 (필요시 customizePendingPayment 구현)
        customizePendingPayment(pending, paymentConfirmRequestDTO);

        pendingPaymentRepository.save(pending);

        log.warn("{} confirm fallback: 결제 임시 저장. orderId={}, reason={}",
            provider, paymentConfirmRequestDTO.getOrderId(), t.getMessage());

        throw new ResponseStatusException(
            HttpStatus.ACCEPTED,
            String.format("%s API 결제 승인 서버의 일시적인 장애로 인해 결제 승인 처리 대기 중입니다.", provider)
        );
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
