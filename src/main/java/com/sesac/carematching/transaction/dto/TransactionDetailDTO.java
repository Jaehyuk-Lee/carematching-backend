package com.sesac.carematching.transaction.dto;

import com.sesac.carematching.transaction.PaymentProvider;
import lombok.Data;

@Data
public class TransactionDetailDTO {
    private PaymentProvider paymentProvider;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
}
