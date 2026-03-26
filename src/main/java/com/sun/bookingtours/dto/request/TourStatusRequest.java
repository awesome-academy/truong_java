package com.sun.bookingtours.dto.request;

import com.sun.bookingtours.entity.enums.TourStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TourStatusRequest {

    @NotNull
    private TourStatus status;
}
