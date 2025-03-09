package com.sesac.carematching.transaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TransactionSuccessDTO {
    private UUID transactionId;
    @NotNull
    private String orderId;
    @NotNull
    private Integer price;
}
