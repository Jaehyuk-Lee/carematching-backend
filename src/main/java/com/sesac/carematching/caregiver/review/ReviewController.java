package com.sesac.carematching.caregiver.review;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverService;
import com.sesac.carematching.caregiver.dto.CaregiverDetailDto;
import com.sesac.carematching.caregiver.experience.Experience;
import com.sesac.carematching.caregiver.review.dto.ReviewRequest;
import com.sesac.carematching.caregiver.review.dto.ReviewResponse;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final CaregiverService caregiverService;
    private final TokenAuth tokenAuth;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> ReviewList(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Caregiver caregiver = caregiverService.findByUsername(username);
        List<ReviewResponse> reviews = reviewService.findReviewList(caregiver)
            .stream()
            .map(ReviewResponse::new)
            .toList();
        return ResponseEntity.ok()
            .body(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> findReviewById(@PathVariable Integer id) {
        Review review = reviewService.findById(id);
        return ResponseEntity.ok()
            .body(new ReviewResponse(review));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Integer id,
                                                       @RequestBody ReviewRequest) {
        Review review = reviewService.findById(id);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ReviewResponse(review));
    }

    @PostMapping("/create")
    public ResponseEntity<ReviewResponse> createReview() {

    }
}
