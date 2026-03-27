package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminBookingDetailResponse(

        UUID id,
        String bookingCode,
        BookingStatus status,

        UUID userId,
        String userFullName,
        String userEmail,
        String userPhone,

        UUID scheduleId,
        String tourTitle,
        String tourSlug,
        LocalDate departureDate,

        int numParticipants,
        BigDecimal totalAmount,
        LocalDateTime bookedAt,
        LocalDateTime cancelledAt,
        String cancelReason,

        // null nếu chưa thanh toán
        PaymentResponse payment

) {}
