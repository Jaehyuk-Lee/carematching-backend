package com.sesac.carematching.transaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionVerifyDTO {
    @NotNull
    private String orderId;
    @NotNull
    private Integer price;
}
