package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddCaregiverDto {
    @NotBlank(message = "사는 지역은 필수 입력 사항입니다.")
    private String loc;
    @NotBlank(message = "실명은 필수 입력 사항입니다.")
    private String realName;
    @NotBlank(message = "전문 분야는 필수 입력 사항입니다.")
    private String servNeeded;
    @NotBlank(message = "근무 요일은 필수 입력 사항입니다.")
    private String workDays;
    private WorkTime workTime;
    private WorkForm workForm;
    private EmploymentType employmentType;
    @NotBlank(message = "봉급은 필수 입력 사항입니다.")
    private Integer salary;
    private Status status;
}
