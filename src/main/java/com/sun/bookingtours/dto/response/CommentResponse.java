package com.sun.bookingtours.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID userId,
        String fullName,
        String avatarUrl,
        String content,
        LocalDateTime createdAt,
        List<CommentResponse> replies   // chỉ populated ở top-level, replies không có replies tiếp
) {}
