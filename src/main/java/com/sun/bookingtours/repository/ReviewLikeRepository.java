package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    boolean existsByUserIdAndReviewId(UUID userId, UUID reviewId);

    Optional<ReviewLike> findByUserIdAndReviewId(UUID userId, UUID reviewId);
}
