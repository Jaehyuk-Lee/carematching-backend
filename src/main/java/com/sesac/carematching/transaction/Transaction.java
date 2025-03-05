package com.sesac.carematching.transaction;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
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
    @Column(name = "transaction_id")
    private UUID transactionId;

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

    @NotNull
    @ColumnDefault("0")
    @Column(name = "PAID", nullable = false)
    private Boolean paid = false;

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

}
