package com.sesac.carematching.transaction.dto

import com.sesac.carematching.transaction.payment.PaymentProvider

data class SelectPgResponseDTO (
    private val pg: PaymentProvider
)
