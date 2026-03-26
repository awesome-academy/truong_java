package com.sun.bookingtours.service;

import com.sun.bookingtours.entity.ReviewLike;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.repository.ReviewLikeRepository;
import com.sun.bookingtours.repository.ReviewRepository;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public void like(UserPrincipal principal, UUID reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review", reviewId);
        }

        if (reviewLikeRepository.existsByUserIdAndReviewId(principal.getId(), reviewId)) {
            throw new BusinessException("Bạn đã like review này rồi");
        }

        try {
            // saveAndFlush thay vì save — force flush ngay để catch được DataIntegrityViolationException
            // nếu dùng save(), Hibernate chỉ flush khi commit (sau khi method return) → exception ra ngoài try-catch
            reviewLikeRepository.saveAndFlush(ReviewLike.builder()
                    .user(userRepository.getReferenceById(principal.getId()))
                    .review(reviewRepository.getReferenceById(reviewId))
                    .build());
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMostSpecificCause().getMessage();
            // Chỉ translate đúng UNIQUE (user_id, review_id) — các violation khác (vd: FK review bị xóa concurrently) thì rethrow
            if (msg != null && msg.contains("review_likes_user_id_review_id_key")) {
                throw new BusinessException("Bạn đã like review này rồi");
            }
            throw e;
        }
    }

    @Transactional
    public void unlike(UserPrincipal principal, UUID reviewId) {
        ReviewLike like = reviewLikeRepository
                .findByUserIdAndReviewId(principal.getId(), reviewId)
                .orElseThrow(() -> new BusinessException("Bạn chưa like review này"));

        reviewLikeRepository.delete(like);
    }
}
