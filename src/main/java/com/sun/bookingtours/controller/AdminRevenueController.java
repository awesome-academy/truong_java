package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.RevenueOverviewResponse;
import com.sun.bookingtours.dto.response.TourRevenueResponse;
import com.sun.bookingtours.service.AdminRevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRevenueController {

    private final AdminRevenueService adminRevenueService;

    @GetMapping
    public ResponseEntity<ApiResponse<RevenueOverviewResponse>> getOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(ApiResponse.success(adminRevenueService.getOverview(from, to)));
    }

    @GetMapping("/tours")
    public ResponseEntity<ApiResponse<List<TourRevenueResponse>>> getTopTours(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(ApiResponse.success(adminRevenueService.getTopTours(from, to, limit)));
    }
}
