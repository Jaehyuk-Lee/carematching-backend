package com.sesac.carematching.caregiver.review;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverService;
import com.sesac.carematching.caregiver.review.dto.ReviewRequest;
import com.sesac.carematching.caregiver.review.dto.ReviewResponse;
import com.sesac.carematching.util.TokenAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review Controller", description = "돌봄이 후기 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final CaregiverService caregiverService;
    private final TokenAuth tokenAuth;

    @Operation(summary = "내 후기 리스트 조회", description = "로그인한 돌봄이의 후기 리스트를 조회합니다.")
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

    @Operation(summary = "내 후기 단건 조회", description = "로그인한 사용자의 후기를 조회합니다.")
    @GetMapping("/user")
    public ResponseEntity<ReviewResponse> findReviewByUser(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Review review = reviewService.findByUser(username);
        return ResponseEntity.ok()
            .body(new ReviewResponse(review));
    }

    @Operation(summary = "후기 등록/수정", description = "후기를 등록하거나 수정합니다.")
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
