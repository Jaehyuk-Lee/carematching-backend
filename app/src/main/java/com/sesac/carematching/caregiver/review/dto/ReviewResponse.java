package com.sesac.carematching.caregiver.review.dto;

import com.sesac.carematching.caregiver.review.Review;

public class ReviewResponse {
    Integer stars;
    String comment;

    public ReviewResponse(Review review) {
        this.stars = review.getStars();
        this.comment = review.getComment();
    }
}
