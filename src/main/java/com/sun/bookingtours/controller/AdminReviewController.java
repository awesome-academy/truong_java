package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.response.AdminReviewResponse;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.entity.enums.TargetType;
import com.sun.bookingtours.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminReviewResponse>>> listReviews(
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) TargetType targetType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                adminReviewService.listReviews(isApproved, targetType, pageable)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AdminReviewResponse>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminReviewService.approveReview(id)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AdminReviewResponse>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminReviewService.rejectReview(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        adminReviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
