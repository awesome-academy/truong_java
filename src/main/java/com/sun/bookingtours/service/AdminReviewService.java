package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.response.AdminReviewResponse;
import com.sun.bookingtours.entity.Review;
import com.sun.bookingtours.entity.enums.TargetType;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.AdminReviewMapper;
import com.sun.bookingtours.repository.ReviewRepository;
import com.sun.bookingtours.repository.ReviewSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final AdminReviewMapper adminReviewMapper;

    @Transactional(readOnly = true)
    public Page<AdminReviewResponse> listReviews(Boolean isApproved, TargetType targetType, Pageable pageable) {
        Specification<Review> spec = Specification
                .where(ReviewSpecification.isApproved(isApproved))
                .and(ReviewSpecification.hasTargetType(targetType));

        return reviewRepository.findAll(spec, pageable).map(adminReviewMapper::toResponse);
    }

    @Transactional
    public AdminReviewResponse approveReview(UUID reviewId) {
        Review review = reviewRepository.findByIdForUpdate(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        review.setApproved(true);
        review.setRejectedAt(null); // xóa dấu reject nếu trước đó đã bị reject

        return adminReviewMapper.toResponse(reviewRepository.save(review));
    }

    @Transactional
    public AdminReviewResponse rejectReview(UUID reviewId) {
        Review review = reviewRepository.findByIdForUpdate(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        review.setApproved(false);
        review.setRejectedAt(LocalDateTime.now());

        return adminReviewMapper.toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        reviewRepository.delete(review);
    }
}
