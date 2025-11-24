package com.sesac.carematching.transaction.dto

import com.sesac.carematching.transaction.payment.PaymentProvider
import com.sesac.carematching.transaction.payment.PgStatus
import com.sesac.carematching.util.fallback.Fallbackable

data class TransactionDetailDTO(
    var paymentProvider: PaymentProvider? = null,
    var paymentKey: String? = null,
    var orderId: String? = null,
    var orderName: String? = null,
    // TossPayments API 기준으로 통일
    // KakaoPay에서는 status 값을 전달해주지 않지만, 200 응답이 올 경우 DONE으로 설정
    var pgStatus: PgStatus? = null,

    // 인터페이스 구현을 생성자 프로퍼티에서 바로 처리
    override var isFallback: Boolean = false
) : Fallbackable
