package com.sesac.carematching.carematch.connect;

import com.sesac.carematching.carematch.request.Request;
import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "CAREMATCH_CONNECT")
public class Connect {
    @Id
    @Column(name = "CMCNO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "CMRNO", nullable = false)
    private Request cmrno;

    @CreatedDate
    @Column(name = "CREATED_AT")
    private Instant createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "UNO", nullable = false)
    private User uno;

    @NotNull
    @Column(name = "CNO", nullable = false)
    private Integer cno;

}
