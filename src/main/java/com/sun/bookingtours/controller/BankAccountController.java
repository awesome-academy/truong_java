package com.sun.bookingtours.controller;

import com.sun.bookingtours.dto.request.CreateBankAccountRequest;
import com.sun.bookingtours.dto.request.UpdateBankAccountRequest;
import com.sun.bookingtours.dto.response.ApiResponse;
import com.sun.bookingtours.dto.response.BankAccountResponse;
import com.sun.bookingtours.security.UserPrincipal;
import com.sun.bookingtours.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(ApiResponse.success(bankAccountService.getAll(principal)));
    }

    @PostMapping
    // 201 Created: thêm mới resource thành công (khác 200 OK dùng cho get/update)
    public ResponseEntity<ApiResponse<BankAccountResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBankAccountRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(bankAccountService.create(principal, request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            // @PathVariable: lấy {id} từ URL path
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBankAccountRequest request) {

        return ResponseEntity.ok(ApiResponse.success(bankAccountService.update(principal, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        bankAccountService.delete(principal, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tài khoản ngân hàng thành công"));
    }

    @PatchMapping("/{id}/default")
    // PATCH: cập nhật một phần resource (khác PUT là cập nhật toàn bộ)
    public ResponseEntity<ApiResponse<BankAccountResponse>> setDefault(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(bankAccountService.setDefault(principal, id)));
    }
}
