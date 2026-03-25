package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.PaymentMethod;
import com.sun.bookingtours.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(

        UUID id,
        UUID bookingId,
        BigDecimal amount,
        String currency,
        PaymentMethod method,
        PaymentStatus status,
        LocalDateTime paidAt

) {}
