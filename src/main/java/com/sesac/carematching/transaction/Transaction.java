package com.sesac.carematching.transaction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "TRANSACTION_ID")
    private UUID transactionId;

    @Column(name = "CNO")
    private Integer cno;

    @Column(name = "UNO")
    private Integer uno;

    @Column(name = "ORDER_ID", length = 50)
    private String orderId;

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

}
