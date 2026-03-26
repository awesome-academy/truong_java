package com.sun.bookingtours.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TourRatingResponse(
        UUID id,
        UUID tourId,
        Short score,
        LocalDateTime createdAt
) {}
