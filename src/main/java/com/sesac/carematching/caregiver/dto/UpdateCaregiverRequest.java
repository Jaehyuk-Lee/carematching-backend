package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateCaregiverRequest {
    private String loc;
    private String realName;
    private String servNeeded;
    private Byte workDays;
    private WorkTime workTime;
    private WorkForm workForm;
    private EmploymentType employmentType;
    private Integer salary;
    private Status status;

    public Caregiver toEntity() {
        return Caregiver.builder()
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
