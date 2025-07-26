package com.sesac.carematching.transaction.pendingPayment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PendingPaymentRepository extends JpaRepository<PendingPayment, Integer> {
    List<PendingPayment> findByConfirmedFalseAndCreatedAtAfter(Instant after);
}
