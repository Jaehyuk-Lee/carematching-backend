package com.sesac.carematching.transaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionConfirmDTO {
    @NotNull
    private String orderId;
    // Nullable (승인 요청에 대한 Response에서만 사용)
    private Integer price;
    // Nullable (KakaoPay에서만 사용)
    private String pgToken;
}
