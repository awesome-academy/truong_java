package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.ScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO trả về thông tin một lịch khởi hành cho client.
 *
 * Không trả thẳng TourSchedule entity vì:
 * - Entity có @ManyToOne Tour — nếu serialize thẳng sẽ kéo theo toàn bộ Tour object (JSON lồng nhau)
 * - DTO kiểm soát chính xác fields nào client nhìn thấy
 */
public record TourScheduleResponse(

        UUID id,
        LocalDate departureDate,
        LocalDate returnDate,
        int totalSlots,

        // null nếu schedule dùng base_price của tour
        BigDecimal priceOverride,

        // OPEN | FULL | CANCELLED
        ScheduleStatus status

) {}
