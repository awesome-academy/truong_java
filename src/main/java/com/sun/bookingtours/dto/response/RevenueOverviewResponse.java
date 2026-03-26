package com.sun.bookingtours.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RevenueOverviewResponse(
        BigDecimal totalRevenue,
        // key = BookingStatus name (String), value = count — dùng String thay vì enum để JSON serialize đẹp hơn
        Map<String, Long> bookingCountByStatus,
        List<MonthlyRevenueResponse> revenueByMonth
) {}
