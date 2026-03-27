package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminBookingResponse(

        UUID id,
        String bookingCode,
        BookingStatus status,

        // User info
        UUID userId,
        String userFullName,
        String userEmail,

        // Tour info
        String tourTitle,
        LocalDate departureDate,

        int numParticipants,
        BigDecimal totalAmount,
        LocalDateTime bookedAt,
        LocalDateTime cancelledAt,
        String cancelReason

) {}
