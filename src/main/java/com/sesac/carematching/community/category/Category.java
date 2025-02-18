package com.sesac.carematching.community.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "COMMUNITY_CATEGORY")
public class Category {
    @Id
    @Column(name = "CPCNO", nullable = false)
    private Integer id;

    @Size(max = 20)
    @NotNull
    @Column(name = "NAME", nullable = false, length = 20)
    private String name;

    @Size(max = 30)
    @Column(name = "ACCESS", length = 30)
    private String access;

}
