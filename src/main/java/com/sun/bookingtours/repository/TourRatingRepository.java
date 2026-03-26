package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.TourRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TourRatingRepository extends JpaRepository<TourRating, UUID> {

    Optional<TourRating> findByUserIdAndTourId(UUID userId, UUID tourId);
}
