package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.TourImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourImageRepository extends JpaRepository<TourImage, UUID> {

    // Validate ảnh thuộc đúng tour trước khi xóa — tránh xóa nhầm ảnh của tour khác
    Optional<TourImage> findByIdAndTourId(UUID id, UUID tourId);
}
