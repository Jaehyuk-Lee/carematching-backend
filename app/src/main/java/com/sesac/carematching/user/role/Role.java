package com.sesac.carematching.user.role;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RNO", nullable = false)
    private Integer id;

    @Size(max = 30)
    @Column(name = "RNAME", length = 30, unique = true)
    private String rname;
}
