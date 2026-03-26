package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.CreatePaymentRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.PaymentResponse;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePaymentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(paymentService.createPayment(principal, request)));
    }

    // GET /api/payments/{bookingId} — xem payment của 1 booking cụ thể
    // Dùng bookingId thay vì paymentId vì client luôn biết bookingId, không biết paymentId
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByBookingId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID bookingId) {

        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getByBookingId(principal, bookingId)));
    }
}
