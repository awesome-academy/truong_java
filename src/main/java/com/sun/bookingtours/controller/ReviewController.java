package com.sun.bookingtours.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sun.bookingtours.dto.request.CreateReviewRequest;
import com.sun.bookingtours.dto.request.UpdateReviewRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.ReviewResponse;
import com.sun.bookingtours.entity.enums.TargetType;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Bỏ class-level @RequestMapping vì controller này phục vụ nhiều base path khác nhau:
// /api/reviews, /api/tours/{slug}/reviews, /api/places/{slug}/reviews, /api/foods/{slug}/reviews
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateReviewRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reviewService.createReview(principal, request)));
    }

    @PutMapping("/api/reviews/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReviewRequest request) {

        return ResponseEntity.ok(ApiResponse.success(reviewService.updateReview(principal, id, request)));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        reviewService.deleteReview(principal, id);
        return ResponseEntity.noContent().build();
    }

    // @PageableDefault → mặc định page=0, size=10, sort theo createdAt mới nhất
    @GetMapping("/api/reviews/me")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(reviewService.getMyReviews(principal, pageable)));
    }

    // ---- Public endpoints (Guest + User) ----

    @GetMapping("/api/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @RequestParam TargetType targetType,
            @RequestParam UUID targetId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviews(targetType, targetId, pageable)));
    }

    @GetMapping("/api/tours/{slug}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getTourReviews(
            @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsByTourSlug(slug, pageable)));
    }

    @GetMapping("/api/places/{slug}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getPlaceReviews(
            @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsByPlaceSlug(slug, pageable)));
    }

    @GetMapping("/api/foods/{slug}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getFoodReviews(
            @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsByFoodSlug(slug, pageable)));
    }
}
