package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.TourScheduleRequest;
import com.sun.bookingtours.dto.request.TourScheduleStatusRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.TourScheduleResponse;
import com.sun.bookingtours.service.TourScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// @RestController = @Controller + @ResponseBody — mọi method tự serialize return value thành JSON
@RestController
@RequiredArgsConstructor
public class TourScheduleController {

    private final TourScheduleService scheduleService;

    @GetMapping("/api/admin/tours/{tourId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TourScheduleResponse>>> list(@PathVariable UUID tourId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.listByTour(tourId)));
    }

    @PostMapping("/api/admin/tours/{tourId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourScheduleResponse>> create(
            @PathVariable UUID tourId,
            @Valid @RequestBody TourScheduleRequest request) {  // @Valid — kích hoạt Bean Validation (@NotNull, @Positive...)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(scheduleService.create(tourId, request)));
    }

    @PutMapping("/api/admin/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourScheduleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TourScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.update(id, request)));
    }

    // PATCH — partial update, chỉ đổi status, không thay toàn bộ resource
    @PatchMapping("/api/admin/schedules/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourScheduleResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TourScheduleStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.updateStatus(id, request)));
    }
}
