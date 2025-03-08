package com.sesac.carematching.review;

import com.sesac.carematching.review.dto.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public Review save(ReviewDto dto) {
        return reviewRepository.save(toEntity(dto));
    }

    private Review toEntity(ReviewDto dto) {
        return Review.builder()
            .stars(dto.getStars())
            .comment(dto.getComment())
            .build();
    }
}
