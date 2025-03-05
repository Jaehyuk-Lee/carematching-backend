package com.sesac.carematching.transaction.dto;

import lombok.Data;

@Data
public class TransactionGetDTO {
    private String caregiverName;
    private String userName;
    private Integer price;
}
