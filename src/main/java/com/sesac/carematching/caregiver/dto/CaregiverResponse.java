package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.*;
import lombok.Getter;

@Getter
public class CaregiverResponse {
    private String loc;
    private String servNeeded;
    private Byte workDays;
    private WorkTime workTime;
    private WorkForm workForm;
    private EmploymentType employmentType;
    private Integer salary;

    public CaregiverResponse(Caregiver caregiver) {
        this.loc = caregiver.getLoc();
        this.servNeeded = caregiver.getServNeeded();
        this.workDays = caregiver.getWorkDays();
        this.workTime = caregiver.getWorkTime();
        this.workForm = caregiver.getWorkForm();
        this.employmentType = caregiver.getEmploymentType();
        this.salary = caregiver.getSalary();
    }
}
