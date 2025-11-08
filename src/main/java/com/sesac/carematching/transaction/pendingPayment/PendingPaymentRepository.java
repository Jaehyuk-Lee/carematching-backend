package com.sesac.carematching.transaction.pendingPayment;

import com.sesac.carematching.transaction.enums.PaymentProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PendingPaymentRepository extends JpaRepository<PendingPayment, Integer> {

    // PendingPayment에 들어있는 엔티티를 특정 크기 단위만큼 가져오기 위한 메서드 (pendingPaymentRetryExecutor에서 사용함)
    List<PendingPayment> findByPaymentProviderAndConfirmedFalseAndCreatedAtAfterAndIdGreaterThan(
        PaymentProvider paymentProvider, // 찾으려는 PG사 이름
        Instant expireLimit, // 시간이 만료된 결제는 처리하지 않음
        Long lastId, // 마지막 처리한 ID
        Pageable pageable // Sorting을 위한 Pageable 객체
    );
}
