package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.*;
import lombok.Getter;

@Getter
public class CaregiverDetailDto {
    private String loc;
    private String realName;
    private String servNeeded;
    private String workDays;
    private WorkTime workTime;
    private WorkForm workForm;
    private EmploymentType employmentType;
    private Integer salary;
    private Status status;

    public CaregiverDetailDto(Caregiver caregiver) {
        this.loc = caregiver.getLoc();
        this.realName = caregiver.getRealName();
        this.servNeeded = caregiver.getServNeeded();
        this.workDays = caregiver.getWorkDays();
        this.workTime = caregiver.getWorkTime();
        this.workForm = caregiver.getWorkForm();
        this.employmentType = caregiver.getEmploymentType();
        this.salary = caregiver.getSalary();
        this.status = caregiver.getStatus();
    }
}
