package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.EmploymentType;
import com.sesac.carematching.caregiver.WorkForm;
import com.sesac.carematching.caregiver.WorkTime;
import lombok.Getter;

@Getter
public class CaregiverListDto {
    private Integer id;
    private String loc;
    private String realName;
    private String servNeeded;
    private String workDays;
    private Integer salary;
    private WorkTime workTime;
    private WorkForm workForm;
    private EmploymentType employmentType;
    private Integer reviewCount;

    public CaregiverListDto(Caregiver caregiver) {
        this.id = caregiver.getId();
        this.loc = caregiver.getLoc();
        this.realName = caregiver.getRealName();
        this.servNeeded = caregiver.getServNeeded();
        this.workDays = caregiver.getWorkDays();
        this.salary = caregiver.getSalary();
        this.workTime = caregiver.getWorkTime();
        this.workForm = caregiver.getWorkForm();
        this.employmentType = caregiver.getEmploymentType();
    }

    public CaregiverListDto(Caregiver caregiver, Integer reviewCount) {
        this.id = caregiver.getId();
        this.loc = caregiver.getLoc();
        this.realName = caregiver.getRealName();
        this.servNeeded = caregiver.getServNeeded();
        this.workDays = caregiver.getWorkDays();
        this.salary = caregiver.getSalary();
        this.workTime = caregiver.getWorkTime();
        this.workForm = caregiver.getWorkForm();
        this.employmentType = caregiver.getEmploymentType();
        this.reviewCount = reviewCount;
    }
}
