package com.sun.bookingtours.dto.request;

import com.sun.bookingtours.entity.enums.ScheduleStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO chuyên dùng cho endpoint PATCH /schedules/{id}/status.
 *
 * Tách riêng thay vì dùng lại TourScheduleRequest vì:
 * - Endpoint này CHỈ được phép thay đổi status, không cho sửa ngày/slot
 * - Nhận đúng 1 field giảm risk client vô tình gửi data thừa
 * - Thể hiện rõ intent: đây là thao tác "đổi trạng thái", không phải "cập nhật thông tin"
 *
 * ScheduleStatus: OPEN | FULL | CANCELLED
 */
public record TourScheduleStatusRequest(@NotNull ScheduleStatus status) {}
