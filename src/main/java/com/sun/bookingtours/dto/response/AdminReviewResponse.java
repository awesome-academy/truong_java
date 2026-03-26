package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.TargetType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminReviewResponse(

        UUID id,
        TargetType targetType,
        UUID targetId,

        UUID userId,
        String userFullName,
        String userEmail,

        String title,
        String content,
        Short rating,

        boolean isApproved,
        LocalDateTime rejectedAt,
        LocalDateTime createdAt

) {}
