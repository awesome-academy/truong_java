package com.sun.bookingtours.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PlaceResponse {

    private UUID id;
    private String name;
    private String slug;
    private String location;
    private String description;
    private String thumbnailUrl;
    private boolean isActive;
    private LocalDateTime createdAt;
}
