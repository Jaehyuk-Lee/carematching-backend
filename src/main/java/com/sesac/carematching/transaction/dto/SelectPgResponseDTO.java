package com.sesac.carematching.transaction.dto;

import com.sesac.carematching.transaction.payment.PaymentProvider;
import lombok.Data;

@Data
public class SelectPgResponseDTO {
    private PaymentProvider pg;
}
