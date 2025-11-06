package com.sesac.carematching.transaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionOrderAddDTO {
    @NotNull
    private String transactionId;
    @NotNull
    private Integer price;
}
