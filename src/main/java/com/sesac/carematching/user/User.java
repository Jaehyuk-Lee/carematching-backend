package com.sesac.carematching.user;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.user.role.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UNO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RNO", nullable = false)
    private Role role;

    @Size(max = 50)
    @NotNull
    @Column(name = "USERNAME", nullable = false, length = 50)
    private String username;

    @Size(max = 255)
    @NotNull
    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Size(max = 50)
    @NotNull
    @Column(name = "NICKNAME", nullable = false, length = 50)
    private String nickname;

    @Size(max = 12)
    @Column(name = "PHONE", length = 12)
    private String phone;

    @Size(max = 30)
    @Column(name = "CERTNO", length = 30)
    private String certno;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "PENDING", nullable = false)
    private Boolean pending = false;

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "user")
    private Caregiver caregiver;

    public User(String username, String password, String nickname, Role role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

}
