package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateReviewRequest(

        String title,

        String content,

        @Min(1) @Max(5) Short rating

) {}
