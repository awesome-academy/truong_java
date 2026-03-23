package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
}
