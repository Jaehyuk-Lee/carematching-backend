package com.sesac.carematching.caregiver.experience;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExperienceRequest {
    private String location;
    @NotBlank(message = "필수항목입니다")
    private String title;
    @NotBlank(message = "필수항목입니다")
    private String summary;
}
