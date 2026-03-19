package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBankAccountRequest {

    @Size(min = 1)
    private String bankName;

    @Size(min = 1)
    private String accountNumber;

    @Size(min = 1)
    private String accountHolder;
}
