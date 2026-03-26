package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.FoodRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.FoodResponse;
import com.sun.bookingtours.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    // ---- Public (Guest + User) ----

    @GetMapping("/api/foods")
    public ResponseEntity<ApiResponse<List<FoodResponse>>> listActive() {
        return ResponseEntity.ok(ApiResponse.success(foodService.listActive()));
    }

    @GetMapping("/api/foods/{slug}")
    public ResponseEntity<ApiResponse<FoodResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(foodService.getBySlug(slug)));
    }

    // ---- Admin ----

    @PostMapping("/api/admin/foods")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodResponse>> create(
            @Valid @RequestBody FoodRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(foodService.create(request)));
    }

    @PutMapping("/api/admin/foods/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FoodRequest request) {

        return ResponseEntity.ok(ApiResponse.success(foodService.update(id, request)));
    }

    @DeleteMapping("/api/admin/foods/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        foodService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa food thành công"));
    }
}
