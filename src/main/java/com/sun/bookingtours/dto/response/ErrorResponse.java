package com.sun.bookingtours.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private final boolean success = false;
    private int status;
    private String code;
    private String message;
    private List<FieldError> errors;

    // Lỗi validation per-field: { "field": "email", "message": "must not be blank" }
    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String message;
    }
}
