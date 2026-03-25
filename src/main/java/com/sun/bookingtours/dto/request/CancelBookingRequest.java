package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelBookingRequest(

        // @NotBlank → không được null, không được rỗng, không được chỉ có khoảng trắng
        // Khác @NotNull (chỉ check null) và @NotEmpty (chỉ check rỗng)
        @NotBlank @Size(max = 500) String cancelReason

) {}
