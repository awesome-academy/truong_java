package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminCancelBookingRequest(

        @NotBlank(message = "Lý do hủy không được để trống")
        String cancelReason

) {}
