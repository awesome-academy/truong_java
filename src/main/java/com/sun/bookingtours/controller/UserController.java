package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.ChangePasswordRequest;
import com.sun.bookingtours.dto.request.UpdateProfileRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.UserProfileResponse;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            // @AuthenticationPrincipal: lấy UserPrincipal từ SecurityContextHolder
            // JwtAuthenticationFilter đã set vào đó trước khi request đến đây
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(principal)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            // @Valid: kích hoạt validation các annotation trên UpdateProfileRequest (@Size...)
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(principal, request)));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(principal, request);
        // Void: không có data trả về, chỉ báo thành công
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
    }
}
