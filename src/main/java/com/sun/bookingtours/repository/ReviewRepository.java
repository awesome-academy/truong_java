package com.sun.bookingtours.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sun.bookingtours.entity.Review;
import com.sun.bookingtours.entity.enums.TargetType;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Kiểm tra user đã review target này chưa — tương ứng UNIQUE constraint trên DB
    // Dùng exists thay vì findBy → DB chỉ cần tìm 1 row rồi dừng, không fetch toàn bộ entity
    boolean existsByUserIdAndTargetTypeAndTargetId(UUID userId, TargetType targetType, UUID targetId);

Optional<Review> findByIdAndUserId(UUID id, UUID userId);

    Page<Review> findByUserId(UUID userId, Pageable pageable);

    // Dùng chung cho tất cả public endpoints: general query + tour/place/food slug-based
    Page<Review> findByTargetTypeAndTargetIdAndIsApprovedTrue(TargetType targetType, UUID targetId, Pageable pageable);
}
