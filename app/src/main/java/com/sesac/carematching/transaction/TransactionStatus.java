package com.sesac.carematching.transaction;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum TransactionStatus {
    PENDING,  // 결제 대기 중 (사용자가 결제 진행 중)
    PENDING_RETRY, // 결제 재시도 대기 중 (PG사 복구 후 자동 재시도 예정)
    SUCCESS,  // 결제 성공
    FAILED,   // 결제 실패
    CANCELED, // 결제 취소
    REFUNDED; // 환불 완료

    // 상태 전이 규칙을 정의하는 맵
    // Key: 현재 상태, Value: 전이 가능한 다음 상태의 Set
    private static final Map<TransactionStatus, Set<TransactionStatus>> allowedTransitions = new EnumMap<>(TransactionStatus.class);

    static {
        // PENDING 상태에서 변경 가능한 상태들
        allowedTransitions.put(PENDING, EnumSet.of(
            SUCCESS,
            FAILED,
            CANCELED,
            PENDING_RETRY
        ));

        // SUCCESS 상태에서 변경 가능한 상태들
        allowedTransitions.put(SUCCESS, EnumSet.of(
            REFUNDED
        ));

        // PENDING_RETRY 상태에서 변경 가능한 상태들
        allowedTransitions.put(PENDING_RETRY, EnumSet.of(
            SUCCESS,
            FAILED,
            CANCELED
        ));

        // FAILED, CANCELED, REFUNDED는 '최종 상태'로, 다른 상태로 변경 불가
        allowedTransitions.put(FAILED, EnumSet.noneOf(TransactionStatus.class));
        allowedTransitions.put(CANCELED, EnumSet.noneOf(TransactionStatus.class));
        allowedTransitions.put(REFUNDED, EnumSet.noneOf(TransactionStatus.class));
    }

    /**
     * 현재 상태(this)에서 nextState로 변경이 가능한지 확인합니다.
     */
    public boolean canTransitionTo(TransactionStatus nextState) {
        // 같은 상태로의 변경은 항상 허용 (멱등성)
        if (this == nextState) {
            return true;
        }

        // 맵에 정의된 규칙 확인
        return allowedTransitions.get(this).contains(nextState);
    }
}
