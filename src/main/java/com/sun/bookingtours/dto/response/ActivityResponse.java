package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.ActivityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class ActivityResponse {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private UUID bookingId;
    private String bookingCode;
    private ActivityType type;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
