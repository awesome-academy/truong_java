package com.sun.bookingtours.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    // tokenType luôn là "Bearer" — client cần gửi kèm header: Authorization: Bearer <accessToken>
    @Builder.Default
    private String tokenType = "Bearer";
}
