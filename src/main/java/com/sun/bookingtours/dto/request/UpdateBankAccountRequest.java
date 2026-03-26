package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBankAccountRequest {

    // @Size áp dụng khi field không null — partial update nên không dùng @NotBlank
    @Size(min = 2, max = 100)
    private String bankName;

    @Size(min = 5, max = 50)
    private String accountNumber;

    @Size(min = 2, max = 100)
    private String accountHolder;
}
