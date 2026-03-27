package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.CreateCommentRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.CommentResponse;
import com.sun.bookingtours.entity.enums.TargetType;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse comment = commentService.createComment(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        commentService.deleteComment(principal, id);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @RequestParam TargetType targetType,
            @RequestParam UUID targetId
    ) {
        List<CommentResponse> response = commentService.getComments(targetType, targetId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
