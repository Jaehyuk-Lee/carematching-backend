package com.sesac.carematching.transaction.payment.pendingPayment;

import com.sesac.carematching.transaction.Transaction;
import jakarta.persistence.*;
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
    @Column(name = "PPNO", nullable = false)
    private Integer id;

    @OneToOne(mappedBy = "pendingPayment")
    private Transaction transaction;

    @Size(max = 255)
    @Column(name = "FAIL_REASON")
    private String failReason;

    // 카카오페이 "단건 결제 승인" (confirm) 전용
    @Column(name = "PARTNER_USER_ID")
    private Integer partnerUserId;

    // 카카오페이 "단건 결제 승인" (confirm) 전용
    @Column(name = "PG_TOKEN")
    private String pgToken;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;
}
