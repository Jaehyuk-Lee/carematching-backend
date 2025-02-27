package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.EmploymentType;
import com.sesac.carematching.caregiver.WorkForm;
import com.sesac.carematching.caregiver.WorkTime;
import lombok.Getter;

@Getter
public class CaregiverListResponse {
    private Integer id;
    private String loc;
    private String realName;
    private String servNeeded;
    private String workDays;
    private Integer salary;

    public CaregiverListResponse(Caregiver caregiver) {
        this.id = caregiver.getId();
        this.loc = caregiver.getLoc();
        this.realName = caregiver.getRealName();
        this.servNeeded = caregiver.getServNeeded();
        this.workDays = caregiver.getWorkDays();
        this.salary = caregiver.getSalary();
    }
}
