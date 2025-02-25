package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.*;
import com.sesac.carematching.user.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddCaregiverRequest {
    private User user;
    @NotBlank(message = "사는 지역은 필수 입력 사항입니다.")
    private String loc;
    @NotBlank(message = "실명은 필수 입력 사항입니다.")
    private String realName;
    @NotBlank(message = "전문 분야는 필수 입력 사항입니다.")
    private String servNeeded;
    @NotBlank(message = "근무 요일은 필수 입력 사항입니다.")
    private String workDays;
    @NotBlank(message = "근무 시간은 필수 입력 사항입니다.")
    private WorkTime workTime;
    @NotBlank(message = "근무 형태는 필수 입력 사항입니다.")
    private WorkForm workForm;
    @NotBlank(message = "고용 형태는 필수 입력 사항입니다.")
    private EmploymentType employmentType;
    @NotBlank(message = "봉급은 필수 입력 사항입니다.")
    private Integer salary;
    private Status status;

    public Caregiver toEntity() {
        return Caregiver.builder()
            .user(user)
            .loc(loc)
            .realName(realName)
            .servNeeded(servNeeded)
            .workDays(workDays)
            .workTime(workTime)
            .workForm(workForm)
            .employmentType(employmentType)
            .salary(salary)
            .status(status)
            .build();
    }
}
