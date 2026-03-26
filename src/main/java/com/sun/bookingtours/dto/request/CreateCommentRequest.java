package com.sun.bookingtours.dto.request;

import com.sun.bookingtours.entity.enums.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCommentRequest(
        @NotNull TargetType targetType,
        @NotNull UUID targetId,
        UUID parentId,      // null = top-level, có giá trị = reply
        @NotBlank String content
) {}
