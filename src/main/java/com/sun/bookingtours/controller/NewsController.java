package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.NewsRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.NewsResponse;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    // ---- Public (Guest + User) ----

    @GetMapping("/api/news")
    public ResponseEntity<ApiResponse<Page<NewsResponse>>> listPublished(
            @PageableDefault(size = 10, sort = "publishedAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(newsService.listPublished(pageable)));
    }

    // /api/news/{slug} phải đặt sau /api/news nếu không có path conflict
    @GetMapping("/api/news/{slug}")
    public ResponseEntity<ApiResponse<NewsResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(newsService.getBySlug(slug)));
    }

    // ---- Admin ----

    @PostMapping("/api/admin/news")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NewsResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NewsRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newsService.create(principal, request)));
    }

    @PutMapping("/api/admin/news/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NewsResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody NewsRequest request) {

        return ResponseEntity.ok(ApiResponse.success(newsService.update(id, request)));
    }

    @DeleteMapping("/api/admin/news/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        newsService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công"));
    }

    // PATCH dùng cho partial update — publish chỉ thay đổi is_published + published_at, không cần toàn bộ body
    @PatchMapping("/api/admin/news/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NewsResponse>> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(newsService.publish(id)));
    }
}
