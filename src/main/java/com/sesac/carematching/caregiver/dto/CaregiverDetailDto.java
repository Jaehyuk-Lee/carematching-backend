package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.*;
import com.sesac.carematching.experience.Experience;
import com.sesac.carematching.experience.ExperienceResponse;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

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
    private List<ExperienceResponse> experienceResponseList;

    public CaregiverDetailDto(Caregiver caregiver) {
        this.loc = caregiver.getLoc();
        this.realName = caregiver.getRealName();
        this.servNeeded = caregiver.getServNeeded();
        this.workDays = caregiver.getWorkDays();
        this.workTime = caregiver.getWorkTime();
        this.workForm = caregiver.getWorkForm();
        this.employmentType = caregiver.getEmploymentType();
        this.salary = caregiver.getSalary();
    }

    public CaregiverDetailDto(Caregiver caregiver, List<Experience> experiences) {
        this.loc = caregiver.getLoc();
        this.realName = caregiver.getRealName();
        this.servNeeded = caregiver.getServNeeded();
        this.workDays = caregiver.getWorkDays();
        this.workTime = caregiver.getWorkTime();
        this.workForm = caregiver.getWorkForm();
        this.employmentType = caregiver.getEmploymentType();
        this.salary = caregiver.getSalary();
        this.experienceResponseList = experiences.stream()
            .map(ExperienceResponse::new)
            .collect(Collectors.toList());
    }
}
