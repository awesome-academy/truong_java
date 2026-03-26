package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.CancelBookingRequest;
import com.sun.bookingtours.dto.request.CreateBookingRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.BookingResponse;
import com.sun.bookingtours.entity.enums.BookingStatus;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBookingRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(bookingService.createBooking(principal, request)));
    }

    // @PageableDefault → giá trị mặc định nếu client không truyền page/size
    // client truyền: ?page=0&size=10&status=PENDING
    // status=null → lấy tất cả (nhờ trick IS NULL OR trong query)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 10, sort = "bookedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getMyBookings(principal, status, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getMyBookingById(principal, id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody CancelBookingRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                bookingService.cancelBooking(principal, id, request)));
    }
}
