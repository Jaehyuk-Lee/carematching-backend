package com.sesac.carematching.transaction.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.transaction.Transaction;
import com.sesac.carematching.transaction.TransactionRepository;
import com.sesac.carematching.transaction.TransactionStatus;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPaymentService implements PaymentService{
    protected final TransactionRepository transactionRepository;

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
     * 기본적으로 Transaction 엔티티의 TransactionStatus를 PENDING_RETRY로 변경
     * 각 PG사에 알맞게 PendingPayment에 추가 저장 가능 (필요시 customizePendingPayment 구현)
     */
    protected TransactionDetailDTO fallbackForConfirm(PaymentConfirmRequestDTO paymentConfirmRequestDTO, Throwable t) {
        PaymentProvider provider = getPaymentProvider();

        Transaction transaction = transactionRepository.findByOrderId(paymentConfirmRequestDTO.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Fallback: 존재하지 않는 주문 ID에 대한 승인 요청입니다. orderId=" + paymentConfirmRequestDTO.getOrderId()));

        transaction.changeTransactionStatus(TransactionStatus.PENDING_RETRY);
        transaction.setPaymentProvider(provider);
        transaction.setPgPaymentKey(paymentConfirmRequestDTO.getPaymentKey());

        PendingPayment pending = new PendingPayment();

        // 각 PG사에 알맞게 PendingPayment에 추가 저장 (필요시 customizePendingPayment 구현)
        customizePendingPayment(pending, paymentConfirmRequestDTO);

        transaction.setPendingPayment(pending);
        transactionRepository.save(transaction);

        log.warn("{} confirm fallback: 결제 재시도 상태로 전환. orderId={}, reason={}",
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
