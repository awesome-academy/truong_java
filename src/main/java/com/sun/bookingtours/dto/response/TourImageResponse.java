package com.sun.bookingtours.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class TourImageResponse {
    private UUID id;
    private String imageUrl;
    private int sortOrder;
}
