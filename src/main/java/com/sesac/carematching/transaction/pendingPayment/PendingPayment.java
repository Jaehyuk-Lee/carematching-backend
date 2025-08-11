package com.sesac.carematching.transaction.pendingPayment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Table(name = "pending_payment")
public class PendingPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "ORDER_ID", nullable = false, length = 100)
    private String orderId;

    @Size(max = 255)
    @NotNull
    @Column(name = "PAYMENT_KEY", nullable = false)
    private String paymentKey;

    @NotNull
    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "CONFIRMED", nullable = false)
    private Boolean confirmed = false;

    @Size(max = 255)
    @Column(name = "FAIL_REASON")
    private String failReason;

    public PendingPayment(String orderId, String paymentKey, Integer price) {
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.price = price;
        this.confirmed = false;
    }
}
