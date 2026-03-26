package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.RatingRequest;
import com.sun.bookingtours.dto.response.TourRatingResponse;
import com.sun.bookingtours.entity.Booking;
import com.sun.bookingtours.entity.Tour;
import com.sun.bookingtours.entity.TourRating;
import com.sun.bookingtours.entity.enums.BookingStatus;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.TourRatingMapper;
import com.sun.bookingtours.repository.BookingRepository;
import com.sun.bookingtours.repository.TourRatingRepository;
import com.sun.bookingtours.repository.TourRepository;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TourRatingService {

    private final TourRatingRepository tourRatingRepository;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final TourRatingMapper tourRatingMapper;

    @Transactional
    public TourRatingResponse upsertRating(UserPrincipal principal, UUID tourId, RatingRequest request) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));

        Booking booking = bookingRepository
                .findFirstByUserIdAndScheduleTourIdAndStatus(principal.getId(), tourId, BookingStatus.COMPLETED)
                .orElseThrow(() -> new BusinessException("Bạn chưa có booking COMPLETED cho tour này"));

        TourRating rating = tourRatingRepository
                .findByUserIdAndTourId(principal.getId(), tourId)
                .orElseGet(() -> TourRating.builder()
                        .user(booking.getUser())
                        .tour(tour)
                        .booking(booking)
                        .build());

        // Cập nhật score dù là insert hay update
        // DB trigger sẽ tự recalculate tours.avg_rating sau khi row này được save
        rating.setScore(request.score());

        return tourRatingMapper.toResponse(tourRatingRepository.save(rating));
    }

    @Transactional(readOnly = true)
    public TourRatingResponse getMyRating(UserPrincipal principal, UUID tourId) {
        if (!tourRepository.existsById(tourId)) {
            throw new ResourceNotFoundException("Tour", tourId);
        }

        TourRating rating = tourRatingRepository
                .findByUserIdAndTourId(principal.getId(), tourId)
                .orElseThrow(() -> new ResourceNotFoundException("TourRating", tourId));

        return tourRatingMapper.toResponse(rating);
    }
}
