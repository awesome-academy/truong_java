package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TourRequest {

    @NotBlank
    private String title;

    // Nếu không truyền, Service sẽ tự sinh từ title
    // @Pattern chỉ áp dụng khi slug không null — chỉ cho phép chữ thường, số, gạch ngang
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    private String slug;

    private String description;

    private String thumbnailUrl;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal basePrice;

    @NotNull
    @Min(1)
    @Max(365)
    private Integer durationDays;

    @NotNull
    @Min(1)
    @Max(500)
    private Integer maxParticipants;

    private String departureLocation;

    // null = không thuộc category nào
    private UUID categoryId;
}
