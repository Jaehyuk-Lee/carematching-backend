package com.sesac.carematching.transaction.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractPaymentService implements PaymentService{
    @Override
    public abstract TransactionDetailDTO confirmPayment(PaymentConfirmRequestDTO request);

    protected <T> T parsePaymentError(String errorJson, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(errorJson, valueType);
        } catch (Exception ex) {
            log.warn("{} 파싱 실패: {}", valueType.getSimpleName(), errorJson, ex);
            return null;
        }
    }
}
