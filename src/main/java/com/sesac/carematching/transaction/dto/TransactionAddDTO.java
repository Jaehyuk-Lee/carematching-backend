package com.sesac.carematching.transaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionAddDTO {
    @NotNull
    private Integer cno;
    @NotNull
    private Integer uno;
}
