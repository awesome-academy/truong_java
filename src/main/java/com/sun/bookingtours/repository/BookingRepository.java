package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Booking;
import com.sun.bookingtours.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // COALESCE(x, 0) → trả 0 thay vì null khi chưa có booking nào
    // (SUM của tập rỗng trong SQL = NULL, không phải 0)
    @Query("""
            SELECT COALESCE(SUM(b.numParticipants), 0)
            FROM Booking b
            WHERE b.schedule.id = :scheduleId
              AND b.status IN :statuses
            """)
    int sumParticipantsByScheduleAndStatus(
            @Param("scheduleId") UUID scheduleId,
            @Param("statuses") List<BookingStatus> statuses
    );

    // (:status IS NULL OR b.status = :status) → nếu truyền null thì bỏ filter, lấy tất cả
    @Query("""
            SELECT b FROM Booking b
            WHERE b.user.id = :userId
              AND (:status IS NULL OR b.status = :status)
            """)
    Page<Booking> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") BookingStatus status,
            Pageable pageable
    );

    // Tìm booking theo id + userId → đảm bảo user chỉ xem booking của chính mình
    Optional<Booking> findByIdAndUserId(UUID id, UUID userId);
}
