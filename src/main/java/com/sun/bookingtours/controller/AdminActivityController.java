package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.response.ActivityResponse;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.entity.enums.ActivityType;
import com.sun.bookingtours.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> listActivities(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) ActivityType type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                activityService.getAdminActivities(userId, type, pageable)));
    }
}
