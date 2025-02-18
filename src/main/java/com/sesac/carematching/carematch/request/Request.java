package com.sesac.carematching.carematch.request;

import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "CAREMATCH_REQUEST")
public class Request {
    @Id
    @Column(name = "CMRNO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "UNO", nullable = false)
    private User uno;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private Type type;

    @Size(max = 255)
    @NotNull
    @Column(name = "LOC", nullable = false)
    private String loc;

    @Size(max = 255)
    @Column(name = "SERV_NEEDED")
    private String servNeeded;

    @NotNull
    @Column(name = "WORK_DAYS", nullable = false)
    private Byte workDays;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "WORK_TIME", nullable = false)
    private WorkTime workTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "WORK_FORM")
    private WorkForm workForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "EMPLOYMENT_TYPE")
    private EmploymentType employmentType;

    @Column(name = "SALARY", precision = 10, scale = 2)
    private BigDecimal salary;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private Status status;

    @CreatedDate
    @Column(name = "CREATED_AT")
    private Instant createdAt;

}
