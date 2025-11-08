package com.sesac.carematching.transaction.payment.pendingPayment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingPaymentRepository extends JpaRepository<PendingPayment, Integer> {

}
