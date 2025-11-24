package com.sesac.carematching.transaction.dto

import com.sesac.carematching.util.fallback.Fallbackable
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import java.time.Instant

@NoArgsConstructor
@AllArgsConstructor
data class PaymentReadyResponseDTO(
    var nextRedirectPcUrl: String?,
    var tid: String?,
    var createdAt: Instant?,

    override var isFallback: Boolean = false
) : Fallbackable
