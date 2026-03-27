package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.PlaceRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.PlaceResponse;
import com.sun.bookingtours.service.PlaceService;
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
public class PlaceController {

    private final PlaceService placeService;

    // ---- Public (Guest + User) ----

    @GetMapping("/api/places")
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> listActive() {
        return ResponseEntity.ok(ApiResponse.success(placeService.listActive()));
    }

    @GetMapping("/api/places/{slug}")
    public ResponseEntity<ApiResponse<PlaceResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(placeService.getBySlug(slug)));
    }

    // ---- Admin ----

    @PostMapping("/api/admin/places")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlaceResponse>> create(
            @Valid @RequestBody PlaceRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(placeService.create(request)));
    }

    @PutMapping("/api/admin/places/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlaceResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PlaceRequest request) {

        return ResponseEntity.ok(ApiResponse.success(placeService.update(id, request)));
    }

    @DeleteMapping("/api/admin/places/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        placeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa place thành công"));
    }
}
