package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PlaceRequest {

    @NotBlank
    private String name;

    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    private String slug;

    private String location;

    private String description;

    private String thumbnailUrl;

    // Nullable: null = giữ nguyên giá trị cũ (dùng cho update), create mặc định true
    private Boolean isActive;
}
