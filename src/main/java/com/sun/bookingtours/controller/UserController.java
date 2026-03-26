package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.ChangePasswordRequest;
import com.sun.bookingtours.dto.request.UpdateProfileRequest;
import com.sun.bookingtours.dto.response.ActivityResponse;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.UserProfileResponse;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.ActivityService;
import com.sun.bookingtours.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ActivityService activityService;

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

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getMyActivities(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                activityService.getMyActivities(principal.getId(), pageable)));
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
