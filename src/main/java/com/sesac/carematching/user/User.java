package com.sesac.carematching.user;

import com.sesac.carematching.user.role.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "USER")
public class User {
    @Id
    @Column(name = "UNO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "RNO", nullable = false)
    private Role rno;

    @Size(max = 50)
    @NotNull
    @Column(name = "USERNAME", nullable = false, length = 50)
    private String username;

    @Size(max = 255)
    @NotNull
    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Size(max = 12)
    @Column(name = "PHONE", length = 12)
    private String phone;

    @Size(max = 30)
    @Column(name = "CERTNO", length = 30)
    private String certno;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "PENDING", nullable = false)
    private Boolean pending = false;

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

}
