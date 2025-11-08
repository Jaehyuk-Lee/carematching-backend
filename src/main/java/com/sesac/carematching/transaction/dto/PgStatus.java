package com.sesac.carematching.transaction.dto;

public enum PgStatus {
    READY,
    IN_PROGRESS,
    WAITING_FOR_DEPOSIT,
    DONE,
    CANCELED,
    PARTIAL_CANCELED,
    ABORTED,
    EXPIRED,
    // 예외 처리용 (특히 TossPayments 이외 PG 대상)
    UNKNOWN
}
