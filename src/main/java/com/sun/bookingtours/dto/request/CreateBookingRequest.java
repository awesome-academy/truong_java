package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBookingRequest(

        // ID của lịch khởi hành muốn đặt
        @NotNull UUID scheduleId,

        // @Min(1) → phải ít nhất 1 người
        // @Max(50) → giới hạn hợp lý, tránh abuse
        @Min(1) @Max(50) int numParticipants

) {}
