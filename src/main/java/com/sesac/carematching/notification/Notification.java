package com.sesac.carematching.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "NOTIFICATION")
public class Notification {
    @Id
    @Column(name = "NNO", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "UNO", nullable = false)
    private Integer uno;

    @ColumnDefault("0")
    @Column(name = "IS_READ")
    private Boolean isRead;

    @CreatedDate
    @Column(name = "CREATED_AT")
    private Instant createdAt;

}
