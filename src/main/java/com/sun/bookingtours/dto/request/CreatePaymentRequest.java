package com.sun.bookingtours.dto.request;

import com.sun.bookingtours.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull UUID bookingId,

        @NotNull PaymentMethod method,

        // nullable — không bắt buộc phải có bank account
        UUID bankAccountId

) {}
