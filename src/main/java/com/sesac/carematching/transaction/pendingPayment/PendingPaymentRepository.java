package com.sesac.carematching.transaction.pendingPayment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface PendingPaymentRepository extends JpaRepository<PendingPayment, Integer> {
    Page<PendingPayment> findByConfirmedFalseAndCreatedAtAfter(Instant expireLimit, Pageable pageable);
}
