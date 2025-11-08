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
import com.sesac.carematching.transaction.enums.PaymentProvider;

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

    @Size(max = 64)
    @NotNull
    @Column(name = "ORDER_ID", nullable = false, length = 64)
    private String orderId;

    @NotNull
    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @NotNull
    @Column(name = "CONFIRMED", nullable = false)
    private Boolean confirmed = false;

    @Size(max = 255)
    @Column(name = "FAIL_REASON")
    private String failReason;

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

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    public PendingPayment(String orderId, String pgPaymentKey, Integer price, PaymentProvider paymentProvider) {
        this.orderId = orderId;
        this.pgPaymentKey = pgPaymentKey;
        this.price = price;
        this.confirmed = false;
        this.paymentProvider = paymentProvider;
    }
}
