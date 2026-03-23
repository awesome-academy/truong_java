package com.sun.bookingtours.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BankAccountResponse {
    private UUID id;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
