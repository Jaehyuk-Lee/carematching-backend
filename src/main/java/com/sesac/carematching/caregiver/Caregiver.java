package com.sesac.carematching.caregiver;

import com.sesac.carematching.experience.Experience;
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
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "caregiver")
@NoArgsConstructor
public class Caregiver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CNO", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "UNO", nullable = false)
    private User user;

    @Size(max = 255)
    @NotNull
    @Column(name = "REAL_NAME", nullable = false)
    private String realName;

    @Size(max = 255)
    @NotNull
    @Column(name = "LOC", nullable = false)
    private String loc;

    @Size(max = 255)
    @Column(name = "SERV_NEEDED")
    private String servNeeded;

    @NotNull
    @Column(name = "WORK_DAYS", nullable = false)
    private String workDays;

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

    @OneToMany(mappedBy = "caregiver", fetch = FetchType.LAZY)
    private List<Experience> experienceList;

    @Builder
    public Caregiver(User user, String loc, String realName, String servNeeded, String workDays, WorkTime workTime,
                     WorkForm workForm, EmploymentType employmentType, Integer salary, Status status) {
        this.user = user;
        this.loc = loc;
        this.realName = realName;
        this.servNeeded = servNeeded;
        this.workDays = workDays;
        this.workTime = workTime;
        this.workForm = workForm;
        this.employmentType = employmentType;
        this.salary = salary;
        this.status = status;
    }
}
