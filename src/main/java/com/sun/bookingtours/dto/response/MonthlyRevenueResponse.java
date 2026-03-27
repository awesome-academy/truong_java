package com.sun.bookingtours.dto.response;

import java.math.BigDecimal;

// Constructor phải khớp đúng thứ tự với SELECT new ... trong JPQL
public record MonthlyRevenueResponse(
        int year,
        int month,
        BigDecimal revenue
) {}
