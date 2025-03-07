package com.sesac.carematching.caregiver.review;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.review.dto.ReviewDto;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public Review save(ReviewDto dto) {
        return reviewRepository.save(toEntity(dto));
    }

    @Transactional
    public Review update(ReviewDto dto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                ()->new IllegalArgumentException("User is null")
        );
        Review review = reviewRepository.findByUser(user).orElse(null);
        review.setComment(dto.getComment());
        review.setStars(dto.getStars());
        return reviewRepository.save(review);
    }

    public List<Review> findReviewList(Caregiver caregiver) {
        return reviewRepository.findByCaregiver(caregiver);
    }

    public void delete(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
            ()->new IllegalArgumentException("User is null")
        );
        Review review = reviewRepository.findByUser(user).orElse(null);
        reviewRepository.delete(review);
    }

    private Review toEntity(ReviewDto dto) {
        return Review.builder()
            .stars(dto.getStars())
            .comment(dto.getComment())
            .build();
    }
}
