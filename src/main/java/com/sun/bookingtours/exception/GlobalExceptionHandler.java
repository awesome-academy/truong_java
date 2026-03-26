package com.sun.bookingtours.exception;

import com.sun.bookingtours.dto.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 — @Valid fail: trả danh sách lỗi theo từng field
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> ErrorResponse.FieldError.builder()
                        .field(e.getField())
                        .message(e.getDefaultMessage())
                        .build())
                .toList();

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(400)
                        .code("VALIDATION_ERROR")
                        .message("Validation failed")
                        .errors(fieldErrors)
                        .build());
    }

    // 400 — unique/FK/not-null constraint từ DB
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String cause = ex.getMostSpecificCause().getMessage();
        String message = (cause != null && cause.contains("users_email_key"))
                ? "Email already in use"
                : "Data integrity violation";

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(400)
                        .code("DATA_CONFLICT")
                        .message(message)
                        .build());
    }

    // 400 — lỗi business logic (slot hết, booking sai trạng thái...)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(400)
                        .code("BUSINESS_ERROR")
                        .message(ex.getMessage())
                        .build());
    }

    // 401 — chưa xác thực (token thiếu / hết hạn)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.builder()
                        .status(401)
                        .code("UNAUTHORIZED")
                        .message(ex.getMessage())
                        .build());
    }

    // 403 — đã login nhưng không đủ quyền (USER cố vào /api/admin/...)
    // AccessDeniedException do Spring Security ném khi @PreAuthorize fail
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .status(403)
                        .code("FORBIDDEN")
                        .message("Access denied")
                        .build());
    }

    // 404 — resource không tồn tại
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .status(404)
                        .code("NOT_FOUND")
                        .message(ex.getMessage())
                        .build());
    }

    // 409 — race condition / pessimistic lock (nhiều request cùng lúc)
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handlePessimisticLocking(PessimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .status(409)
                        .code("CONFLICT")
                        .message("Thao tác đang được xử lý, vui lòng thử lại")
                        .build());
    }

    // 500 — catch-all: lỗi bất ngờ không thuộc các loại trên
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .status(500)
                        .code("INTERNAL_ERROR")
                        .message("An unexpected error occurred")
                        .build());
    }
}
