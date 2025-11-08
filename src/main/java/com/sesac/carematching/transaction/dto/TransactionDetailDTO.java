package com.sesac.carematching.transaction.dto;

import com.sesac.carematching.transaction.PaymentProvider;
import lombok.Data;

@Data
public class TransactionDetailDTO {
    private PaymentProvider paymentProvider;
    private String paymentKey;
    private String orderId;
    private String orderName;
    // TossPayments API 기준으로 통일
    // KakaoPay에서는 status 값을 전달해주지 않지만, 200 응답이 올 경우 "DONE"으로 설정
    private PgStatus status;
}
