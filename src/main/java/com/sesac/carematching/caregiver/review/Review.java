package com.sesac.carematching.caregiver.review;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.role.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RVNO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UNO", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CNO", nullable = false)
    private Caregiver caregiver;

    @Size(max = 5)
    @NotNull
    @Column(name = "STARS", nullable = false, length = 5)
    private Integer stars;

    @Size(max = 255)
    @NotNull
    @Column(name = "COMMENT")
    private String comment;

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Builder
    public Review(Integer stars, String comment) {
        this.stars = stars;
        this.comment = comment;
    }
}
