package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews/{reviewId}/like")
@RequiredArgsConstructor
public class ReviewLikeController {

    private final ReviewLikeService reviewLikeService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> like(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID reviewId
    ) {
        reviewLikeService.like(principal, reviewId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Liked"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> unlike(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID reviewId
    ) {
        reviewLikeService.unlike(principal, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Unliked"));
    }
}
