package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.*;
import lombok.Getter;

@Getter
public class CaregiverDetailDto {
    private String loc;
    private Integer uno;
    private String realName;
    private String servNeeded;
    private String workDays;
    private WorkTime workTime;
    private WorkForm workForm;
    private EmploymentType employmentType;
    private Integer salary;

    public CaregiverDetailDto(Caregiver caregiver) {
        this.loc = caregiver.getLoc();
        this.uno = caregiver.getUser().getId();
        this.realName = caregiver.getRealName();
        this.servNeeded = caregiver.getServNeeded();
        this.workDays = caregiver.getWorkDays();
        this.workTime = caregiver.getWorkTime();
        this.workForm = caregiver.getWorkForm();
        this.employmentType = caregiver.getEmploymentType();
        this.salary = caregiver.getSalary();
    }
}
