package com.sesac.carematching.transaction.dto

data class PaymentConfirmRequestDTO (
    // 필드명은 Toss 가능하면 Toss 기준으로 작성하였으며,
    // 다른 PG사에서 다른 이름을 사용한다면, 주석으로 작성
    // 공통 필드 (Toss, Kakao 모두 필요) = null 입력 불가
    // Kakao - partner_order_id
    val orderId: String,

    // Kakao - total_amount
    val amount: Int,

    // Kakao - tid
    val paymentKey: String,

    // 여기부터는 각 PG사별 추가 필드
    // PG사별 필드 - Kakao
    var pgToken: String? = null,
    var partnerUserId: String? = null
)
