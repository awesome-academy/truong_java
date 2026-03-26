package com.sun.bookingtours.dto.request;

import com.sun.bookingtours.entity.enums.TargetType;
import com.sun.bookingtours.validation.AllowedTargetTypes;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReviewRequest(

        @NotNull
        @AllowedTargetTypes(value = {TargetType.TOUR, TargetType.PLACE, TargetType.FOOD},
                message = "targetType phải là TOUR, PLACE hoặc FOOD")
        TargetType targetType,

        @NotNull UUID targetId,

        String title,

        String content,

        // @Min/@Max → validate nếu có truyền, nhưng không @NotNull vì rating là optional
        @Min(1) @Max(5) Short rating

) {}
