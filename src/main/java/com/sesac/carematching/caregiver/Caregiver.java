package com.sesac.carematching.caregiver;

import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "caregiver")
@NoArgsConstructor
public class Caregiver {
    @Id
    @Column(name = "UNO", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "UNO", nullable = false)
    private User user;

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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "WORK_FORM", nullable = false)
    private WorkForm workForm;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "EMPLOYMENT_TYPE", nullable = false)
    private EmploymentType employmentType;

    @Column(name = "SALARY")
    private Integer salary;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private Status status;

    @Builder
    public Caregiver(User user, String loc, String servNeeded, Byte workDays, WorkTime workTime, WorkForm workForm,
                     EmploymentType employmentType, Integer salary, Status status) {
        this.user = user;
        this.id = user.getId();
        this.loc = loc;
        this.servNeeded = servNeeded;
        this.workDays = workDays;
        this.workTime = workTime;
        this.workForm = workForm;
        this.employmentType = employmentType;
        this.salary = salary;
        this.status = status;
    }
}
