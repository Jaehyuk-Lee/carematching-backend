package com.sesac.carematching.transaction.payment;

/**
 * 결제 제공자 식별자
 * 결제가 어떤 PG로 처리되었는지 저장
 * PaymentProvider에 선언된 값들의 순서를 기반으로 PG사 선정 우선순위 반영됨
 */
public enum PaymentProvider {
    TOSS,
    KAKAO
}
