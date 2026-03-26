package com.sun.bookingtours.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NewsResponse {

    private UUID id;
    private UUID authorId;
    private String authorName;
    private String title;
    private String slug;
    private String content;
    private String thumbnailUrl;
    private boolean isPublished;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
