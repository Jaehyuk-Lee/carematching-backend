package com.sesac.carematching.caregiver;

import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "caregiver")
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
    @Lob
    @Column(name = "WORK_TIME", nullable = false)
    private String workTime;

    @Lob
    @Column(name = "WORK_FORM")
    private String workForm;

    @Lob
    @Column(name = "EMPLOYMENT_TYPE")
    private String employmentType;

    @Column(name = "SALARY")
    private Integer salary;

    @NotNull
    @Lob
    @Column(name = "STATUS", nullable = false)
    private String status;

    @CreatedDate
    @Column(name = "CREATED_AT")
    private Instant createdAt;

}
