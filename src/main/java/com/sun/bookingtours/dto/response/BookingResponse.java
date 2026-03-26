package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(

        UUID id,
        String bookingCode,

        // Nhúng thêm thông tin schedule/tour để client hiển thị luôn, không cần gọi thêm API
        UUID scheduleId,
        String tourTitle,
        LocalDate departureDate,

        int numParticipants,
        BigDecimal totalAmount,
        BookingStatus status,
        LocalDateTime bookedAt,

        // null nếu chưa huỷ
        LocalDateTime cancelledAt,
        String cancelReason

) {}
