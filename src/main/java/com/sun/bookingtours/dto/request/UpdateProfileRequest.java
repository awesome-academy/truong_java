package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;

    private String avatarUrl;
}
