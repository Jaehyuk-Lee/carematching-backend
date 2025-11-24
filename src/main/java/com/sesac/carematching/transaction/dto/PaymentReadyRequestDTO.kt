package com.sesac.carematching.transaction.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class PaymentReadyRequestDTO (
    val orderId: @NotBlank(message = "주문 ID는 필수입니다.") String,
    val userId: @NotBlank(message = "사용자 ID는 필수입니다.") String,
    val itemName: @NotBlank(message = "상품명은 필수입니다.") String,
    val quantity: @Min(value = 1, message = "수량은 1 이상이어야 합니다.") Int,
    val totalAmount: @Min(value = 1, message = "금액은 1 이상이어야 합니다.") Int
)
