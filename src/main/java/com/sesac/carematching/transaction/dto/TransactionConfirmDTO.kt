package com.sesac.carematching.transaction.dto

data class TransactionConfirmDTO (
    val orderId: String,

    // Nullable (승인 요청에 대한 Response에서만 사용)
    val price: Int? = null,

    // Nullable (KakaoPay에서만 사용)
    val pgToken: String? = null
)
