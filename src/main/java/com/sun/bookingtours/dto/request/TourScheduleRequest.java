package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO nhận data từ client khi tạo hoặc cập nhật một lịch khởi hành (TourSchedule).
 *
 * Dùng record thay vì class vì:
 * - DTO chỉ truyền data một chiều (client → server), không cần thay đổi sau khi tạo
 * - record tự sinh constructor, getter, equals, hashCode — không cần Lombok
 * - Getter của record: departureDate() thay vì getDepartureDate() (không có prefix "get")
 *
 * Bean Validation hoạt động bình thường trên record component —
 * chỉ cần đặt annotation trước type, Controller thêm @Valid là Spring tự validate.
 */
public record TourScheduleRequest(

        // @NotNull — không được phép null, trả 400 nếu thiếu field này
        @NotNull LocalDate departureDate,

        @NotNull LocalDate returnDate,

        // @Positive — phải > 0 (tổng slot không thể bằng 0 hay âm)
        @Positive int totalSlots,

        // nullable — null = dùng base_price của tour
        // BigDecimal thay vì double để tránh lỗi làm tròn khi tính tiền
        BigDecimal priceOverride

) {}
