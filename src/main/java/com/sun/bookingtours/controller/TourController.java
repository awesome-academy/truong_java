package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.TourImageRequest;
import com.sun.bookingtours.dto.request.TourLinksRequest;
import com.sun.bookingtours.dto.request.TourRequest;
import com.sun.bookingtours.dto.request.TourStatusRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.TourResponse;
import com.sun.bookingtours.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    // ---- Admin ----

    @PostMapping("/api/admin/tours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourResponse>> create(@Valid @RequestBody TourRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(tourService.create(request)));
    }

    @PutMapping("/api/admin/tours/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TourRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tourService.update(id, request)));
    }

    @DeleteMapping("/api/admin/tours/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        tourService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tour thành công"));
    }

    // PATCH vì chỉ update 1 field (status), không phải toàn bộ resource
    @PatchMapping("/api/admin/tours/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TourStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tourService.updateStatus(id, request)));
    }

    // Thêm ảnh vào tour — nhận danh sách URL
    // Khi có UI: UI upload file lên storage → nhận URL → gọi endpoint này
    @PostMapping("/api/admin/tours/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourResponse>> addImages(
            @PathVariable UUID id,
            @Valid @RequestBody TourImageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(tourService.addImages(id, request)));
    }

    @DeleteMapping("/api/admin/tours/{id}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId) {
        tourService.deleteImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.success("Xóa ảnh thành công"));
    }

    // Gửi lên toàn bộ danh sách places mới — service sẽ replace hết
    @PutMapping("/api/admin/tours/{id}/places")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourResponse>> updatePlaces(
            @PathVariable UUID id,
            @Valid @RequestBody TourLinksRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tourService.updatePlaces(id, request)));
    }

    @PutMapping("/api/admin/tours/{id}/foods")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TourResponse>> updateFoods(
            @PathVariable UUID id,
            @Valid @RequestBody TourLinksRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tourService.updateFoods(id, request)));
    }
}
