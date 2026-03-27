package com.sun.bookingtours.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record TourRevenueResponse(
        UUID tourId,
        String tourTitle,
        String tourSlug,
        BigDecimal totalRevenue,
        long bookingCount
) {}
