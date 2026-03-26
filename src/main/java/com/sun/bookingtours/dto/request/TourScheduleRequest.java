package com.sun.bookingtours.dto.request;

import com.sun.bookingtours.exception.BusinessException;
import jakarta.validation.constraints.FutureOrPresent;
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

        // @FutureOrPresent — không thể tạo lịch khởi hành trong quá khứ
        @NotNull @FutureOrPresent LocalDate departureDate,

        @NotNull LocalDate returnDate,

        // @Positive — phải > 0 (tổng slot không thể bằng 0 hay âm)
        @Positive int totalSlots,

        // nullable — null = dùng base_price của tour
        // BigDecimal thay vì double để tránh lỗi làm tròn khi tính tiền
        BigDecimal priceOverride

) {
    // Compact constructor — chạy trước Bean Validation nên cần null guard
    // @NotNull sẽ bắt null sau, ở đây chỉ cần validate business rule khi cả 2 đều có giá trị
    public TourScheduleRequest {
        if (departureDate != null && returnDate != null && !returnDate.isAfter(departureDate)) {
            throw new BusinessException("returnDate phải sau departureDate");
        }
        if (priceOverride != null && priceOverride.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("priceOverride phải lớn hơn 0");
        }
    }
}
