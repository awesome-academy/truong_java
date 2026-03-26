package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.AdminCancelBookingRequest;
import com.sun.bookingtours.dto.response.AdminBookingDetailResponse;
import com.sun.bookingtours.dto.response.AdminBookingResponse;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.entity.enums.BookingStatus;
import com.sun.bookingtours.service.AdminBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    // @DateTimeFormat(iso = DATE) → Spring tự parse chuỗi "2024-01-15" thành LocalDate
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminBookingResponse>>> listBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID userId,
            @PageableDefault(size = 20, sort = "bookedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                adminBookingService.listBookings(status, fromDate, toDate, userId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminBookingDetailResponse>> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminBookingService.getBookingDetail(id)));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminBookingService.completeBooking(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody AdminCancelBookingRequest request) {

        return ResponseEntity.ok(ApiResponse.success(adminBookingService.cancelBooking(id, request)));
    }
}
