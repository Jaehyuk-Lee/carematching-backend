package com.sesac.carematching.transaction;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.transaction.exception.IllegalTransactionStateException;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPayment;
import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "transaction",
        indexes = @Index(name = "idx_transaction_status_provider_created", columnList = "STATUS, PAYMENT_PROVIDER, CREATED_AT"),
        uniqueConstraints = @UniqueConstraint(name = "uk_payment_provider_pg_payment_key", columnNames = {"PAYMENT_PROVIDER", "PG_PAYMENT_KEY"})
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TNO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CNO", nullable = false)
    private Caregiver cno;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UNO", nullable = false)
    private User uno;

    @Size(max = 64)
    @Column(name = "ORDER_ID", nullable = false, unique = true, updatable = false, length = 64)
    private String orderId;

    @Column(name = "ORDER_NAME")
    private String orderName;

    @NotNull
    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    @Setter(AccessLevel.NONE)
    private TransactionStatus transactionStatus = TransactionStatus.PENDING;

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_PROVIDER")
    private PaymentProvider paymentProvider;

    // PG사에서 발급한 고유 거래 ID
    // (PG사마다 부르는 이름이 다름)
    // 토스페이먼츠: paymentKey
    // 카카오페이: tid
    @Column(name = "PG_PAYMENT_KEY")
    private String pgPaymentKey;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "PPNO")
    private PendingPayment pendingPayment;

    public void setPendingPayment(PendingPayment pendingPayment) {
        this.pendingPayment = pendingPayment;
        if (pendingPayment != null && pendingPayment.getTransaction() != this) {
            pendingPayment.setTransaction(this);
        }
    }

    /**
     * 트랜잭션의 상태를 변경합니다.
     * 이 메서드는 정의된 상태 전이 규칙(State Machine)을 따릅니다.
     * @param nextState 변경하려는 다음 상태
     * @throws IllegalTransactionStateException 규칙에 어긋나는 상태 변경 시
     */
    public void changeTransactionStatus(TransactionStatus nextState) {
        if (!this.transactionStatus.canTransitionTo(nextState)) {
            throw new IllegalTransactionStateException(
                "잘못된 상태 변경입니다: " + this.transactionStatus + " -> " + nextState +
                    " (OrderId: " + this.orderId + ")"
            );
        }

        // 규칙 통과 시 상태 변경
        this.transactionStatus = nextState;
    }

    @PrePersist
    public void generateUuid() {
        if (this.orderId == null) {
            this.orderId = UUID.randomUUID().toString();
        }
    }
}
