package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.TargetType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(

        UUID id,
        UUID userId,
        TargetType targetType,
        UUID targetId,

        // null nếu review không gắn với booking cụ thể
        UUID bookingId,

        String title,
        String content,
        Short rating,
        boolean isApproved,
        LocalDateTime createdAt

) {}
