package com.sesac.carematching.transaction;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TNO", nullable = false)
    private Integer id;

    @Size(max = 64)
    @Column(name = "ORDER_ID", nullable = false, unique = true, updatable = false, length = 64)
    private String orderId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CNO", nullable = false)
    private Caregiver cno;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UNO", nullable = false)
    private User uno;

    @NotNull
    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @Column(name = "PAID_PRICE")
    private Integer paidPrice;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private Status status = Status.PENDING;

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_PROVIDER", nullable = false)
    private PaymentProvider paymentProvider;

    // PG사에서 발급한 고유 거래 ID
    // (PG사마다 부르는 이름이 다름)
    // 토스페이먼츠: paymentKey
    // 카카오페이: tid
    @Column(name = "PG_PAYMENT_KEY")
    private String pgPaymentKey;

    @PrePersist
    public void generateUuid() {
        if (this.orderId == null) {
            this.orderId = UUID.randomUUID().toString();
        }
    }
}
