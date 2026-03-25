package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TourRequest {

    @NotBlank
    private String title;

    // Nếu không truyền, Service sẽ tự sinh từ title
    private String slug;

    private String description;

    private String thumbnailUrl;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal basePrice;

    @NotNull
    @Min(1)
    private Integer durationDays;

    @NotNull
    @Min(1)
    private Integer maxParticipants;

    private String departureLocation;

    // null = không thuộc category nào
    private UUID categoryId;
}
