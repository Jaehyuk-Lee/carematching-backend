package com.sesac.carematching.caregiver.experience;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExperienceResponse {
    private String location;
    private String title;
    private String summary;

    public ExperienceResponse(Experience experience) {
        this.location = experience.getLocation();
        this.title = experience.getTitle();
        this.summary = experience.getSummary();
    }
}
