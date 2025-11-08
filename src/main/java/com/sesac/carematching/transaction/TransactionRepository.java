package com.sesac.carematching.transaction;

import com.sesac.carematching.transaction.payment.PaymentProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    Optional<Transaction> findByOrderId(String orderId);

    List<Transaction> findByTransactionStatusAndPaymentProviderAndCreatedAtAfterAndIdGreaterThan(
            TransactionStatus transactionStatus,
            PaymentProvider paymentProvider,
            Instant createdAt,
            Integer id,
            Pageable pageable
    );
}
