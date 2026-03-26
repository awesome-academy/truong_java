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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    // ---- Public (Guest + User) ----

    // @RequestParam(required = false) — param tùy chọn, không gửi lên thì Spring inject null
    // @PageableDefault — giá trị mặc định nếu client không truyền page/size/sort
    @GetMapping("/api/tours")
    public ResponseEntity<ApiResponse<Page<TourResponse>>> listPublic(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer durationDays,
            @RequestParam(required = false) String departureLocation,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                tourService.listPublic(categoryId, minPrice, maxPrice, durationDays, departureLocation, pageable)));
    }

    // /api/tours/search phải đặt TRƯỚC /api/tours/{slug}
    // Nếu đặt sau, Spring sẽ match "search" như 1 slug → gọi nhầm getDetail("search")
    @GetMapping("/api/tours/search")
    public ResponseEntity<ApiResponse<Page<TourResponse>>> search(
            @RequestParam String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(tourService.search(q, pageable)));
    }

    @GetMapping("/api/tours/{slug}")
    public ResponseEntity<ApiResponse<TourResponse>> getDetail(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(tourService.getPublicDetail(slug)));
    }

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
