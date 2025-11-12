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
    private boolean isFallback;

    public PaymentReadyResponseDTO (String nextRedirectPcUrl, String tid, Instant createdAt) {
        this.nextRedirectPcUrl = nextRedirectPcUrl;
        this.tid = tid;
        this.createdAt = createdAt;
        this.isFallback = false;
    }
}
