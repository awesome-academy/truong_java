package com.sun.bookingtours.repository;

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

import com.sun.bookingtours.entity.Review;
import com.sun.bookingtours.entity.enums.BookingStatus;
import com.sun.bookingtours.entity.enums.TargetType;

import jakarta.persistence.LockModeType;

public interface ReviewRepository extends JpaRepository<Review, UUID>, JpaSpecificationExecutor<Review> {

    // Kiểm tra user đã review target này chưa — tương ứng UNIQUE constraint trên DB
    // Dùng exists thay vì findBy → DB chỉ cần tìm 1 row rồi dừng, không fetch toàn bộ entity
    boolean existsByUserIdAndTargetTypeAndTargetId(UUID userId, TargetType targetType, UUID targetId);

    // Kiểm tra user có booking COMPLETED cho tour không
    // Join chain: Booking → schedule (TourSchedule) → tour (Tour)
    @Query("""
            SELECT COUNT(b) > 0
            FROM Booking b
            WHERE b.user.id = :userId
              AND b.schedule.tour.id = :tourId
              AND b.status = :status
            """)
    boolean existsBookingByUserAndTourAndStatus(
            @Param("userId") UUID userId,
            @Param("tourId") UUID tourId,
            @Param("status") BookingStatus status
    );

    Optional<Review> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findByIdForUpdate(@Param("id") UUID id);

    // @EntityGraph → JOIN FETCH user trong 1 query, tránh N+1 khi list reviews
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findAll(Specification<Review> spec, Pageable pageable);

    Page<Review> findByUserId(UUID userId, Pageable pageable);

    // Dùng chung cho tất cả public endpoints: general query + tour/place/food slug-based
    Page<Review> findByTargetTypeAndTargetIdAndIsApprovedTrue(TargetType targetType, UUID targetId, Pageable pageable);
}
