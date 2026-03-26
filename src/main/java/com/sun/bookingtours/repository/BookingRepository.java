package com.sun.bookingtours.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sun.bookingtours.entity.Booking;
import com.sun.bookingtours.entity.enums.BookingStatus;

import jakarta.persistence.LockModeType;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    // COALESCE(x, 0) → trả 0 thay vì null khi chưa có booking nào
    // (SUM của tập rỗng trong SQL = NULL, không phải 0)
    @Query("""
            SELECT COALESCE(SUM(b.numParticipants), 0)
            FROM Booking b
            WHERE b.schedule.id = :scheduleId
              AND b.status IN :statuses
            """)
    long sumParticipantsByScheduleAndStatus(
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.user.id = :userId")
    Optional<Booking> findByIdAndUserIdForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") UUID id);

    // @EntityGraph → JOIN FETCH user, schedule, tour trong 1 query, tránh N+1
    @EntityGraph(attributePaths = {"user", "schedule", "schedule.tour"})
    Page<Booking> findAll(Specification<Booking> spec, Pageable pageable);

    Optional<Booking> findFirstByUserIdAndScheduleTourIdAndStatus(UUID userId, UUID tourId, BookingStatus status);

    List<Booking> findTop5ByUserIdOrderByBookedAtDesc(UUID userId);
}
