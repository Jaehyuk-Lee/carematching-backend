package com.sesac.carematching.user.role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ROLE")
public class Role {
    @Id
    @Column(name = "RNO", nullable = false)
    private Integer id;

    @Size(max = 30)
    @Column(name = "RNAME", length = 30)
    private String rname;

}
