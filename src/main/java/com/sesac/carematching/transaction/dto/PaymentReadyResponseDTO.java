package com.sesac.carematching.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReadyResponseDTO {
    private String nextRedirectPcUrl;
    private String tid;
    private Instant createdAt;
}
