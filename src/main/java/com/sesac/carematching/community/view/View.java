package com.sesac.carematching.community.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "COMMUNITY_VIEW")
public class View {
    @Id
    @Column(name = "CVNO", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "UNO", nullable = false)
    private Integer uno;

    @NotNull
    @Column(name = "CPNO", nullable = false)
    private Integer cpno;

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

}
