package com.sesac.carematching.caregiver.review;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverService;
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

    @GetMapping("/user")
    public ResponseEntity<ReviewResponse> findReviewByUser(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Review review = reviewService.findByUser(username);
        return ResponseEntity.ok()
            .body(new ReviewResponse(review));
    }

    @PostMapping("/build")
    public ResponseEntity<ReviewResponse> updateReview(HttpServletRequest request,
                                                       @RequestBody ReviewRequest dto) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Review review;
        if (reviewService.findByUser(username) == null) {
            review = reviewService.save(dto);
        } else {
            review = reviewService.update(username, dto);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ReviewResponse(review));
    }
}
