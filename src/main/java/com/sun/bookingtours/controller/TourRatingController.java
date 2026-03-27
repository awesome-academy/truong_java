package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.RatingRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.TourRatingResponse;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.TourRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tours/{tourId}/ratings")
@RequiredArgsConstructor
public class TourRatingController {

    private final TourRatingService tourRatingService;

    @PostMapping
    public ResponseEntity<ApiResponse<TourRatingResponse>> upsertRating(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID tourId,
            @Valid @RequestBody RatingRequest request
    ) {
        TourRatingResponse response = tourRatingService.upsertRating(principal, tourId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TourRatingResponse>> getMyRating(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID tourId
    ) {
        TourRatingResponse response = tourRatingService.getMyRating(principal, tourId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
