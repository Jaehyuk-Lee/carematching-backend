package com.sesac.carematching.caregiver.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {
    @NotBlank(message = "별점은 필수입니다.")
    Integer stars;
    String comment;
}
