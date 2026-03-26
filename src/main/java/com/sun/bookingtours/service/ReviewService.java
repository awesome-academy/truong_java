package com.sun.bookingtours.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.bookingtours.dto.request.CreateReviewRequest;
import com.sun.bookingtours.dto.request.UpdateReviewRequest;
import com.sun.bookingtours.dto.response.ReviewResponse;
import com.sun.bookingtours.entity.Booking;
import com.sun.bookingtours.entity.Review;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.entity.enums.ActivityType;
import com.sun.bookingtours.entity.enums.BookingStatus;
import com.sun.bookingtours.entity.enums.TargetType;
import com.sun.bookingtours.event.ActivityLogEvent;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.ReviewMapper;
import com.sun.bookingtours.repository.BookingRepository;
import com.sun.bookingtours.repository.ReviewRepository;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReviewResponse createReview(UserPrincipal principal, CreateReviewRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));

        if (reviewRepository.existsByUserIdAndTargetTypeAndTargetId(user.getId(), request.targetType(), request.targetId())) {
            throw new BusinessException("Bạn đã review target này rồi");
        }

        Booking booking = null;

        if (request.targetType() == TargetType.TOUR) {
            booking = bookingRepository.findFirstByUserIdAndScheduleTourIdAndStatus(
                            user.getId(), request.targetId(), BookingStatus.COMPLETED)
                    .orElseThrow(() -> new BusinessException("Bạn chưa có booking COMPLETED cho tour này"));
        }

        Review review = Review.builder()
                .user(user)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .booking(booking)
                .title(request.title())
                .content(request.content())
                .rating(request.rating())
                .build();

        Review saved = reviewRepository.save(review);

        // booking có thể null (PLACE/FOOD review) → Activity.booking là nullable → OK
        eventPublisher.publishEvent(new ActivityLogEvent(user, booking, ActivityType.REVIEW_CREATED));

        return reviewMapper.toResponse(saved);
    }

    @Transactional
    public ReviewResponse updateReview(UserPrincipal principal, UUID reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        if (review.isApproved()) {
            throw new BusinessException("Không thể chỉnh sửa review đã được duyệt");
        }

        // Chỉ update field nào client có truyền (non-null) — tránh ghi đè thành null
        if (request.title() != null)   review.setTitle(request.title());
        if (request.content() != null) review.setContent(request.content());
        if (request.rating() != null)  review.setRating(request.rating());
        // Không cần save() — dirty checking tự sinh UPDATE khi transaction commit

        return reviewMapper.toResponse(review);
    }

    @Transactional
    public void deleteReview(UserPrincipal principal, UUID reviewId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        reviewRepository.delete(review);
    }

    // readOnly = true → Hibernate bỏ dirty checking → tiết kiệm memory
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(UserPrincipal principal, Pageable pageable) {
        return reviewRepository.findByUserId(principal.getId(), pageable)
                .map(reviewMapper::toResponse);
    }
}
