package com.sun.bookingtours.dto.request;

import com.sun.bookingtours.entity.enums.TargetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReviewRequest(

        @NotNull TargetType targetType,

        @NotNull UUID targetId,

        String title,

        String content,

        // @Min/@Max → validate nếu có truyền, nhưng không @NotNull vì rating là optional
        @Min(1) @Max(5) Short rating

) {}
