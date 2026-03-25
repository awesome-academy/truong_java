package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Tour;
import com.sun.bookingtours.entity.enums.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<Tour, UUID> {

    boolean existsBySlug(String slug);

    // Tìm tour theo slug, JOIN FETCH images + places + foods trong 1 query
    // Tránh LazyInitializationException khi serialize response ngoài transaction
    @Query("""
        SELECT t FROM Tour t
        LEFT JOIN FETCH t.images
        LEFT JOIN FETCH t.places
        LEFT JOIN FETCH t.foods
        WHERE t.slug = :slug AND t.deletedAt IS NULL
    """)
    Optional<Tour> findBySlugWithDetails(@Param("slug") String slug);

    // Tìm theo id — dùng cho admin (không cần filter deleted)
    @Query("""
        SELECT t FROM Tour t
        LEFT JOIN FETCH t.images
        LEFT JOIN FETCH t.places
        LEFT JOIN FETCH t.foods
        WHERE t.id = :id
    """)
    Optional<Tour> findByIdWithDetails(@Param("id") UUID id);

    // Public list: chỉ ACTIVE, chưa bị xóa, có pagination
    Page<Tour> findByStatusAndDeletedAtIsNull(TourStatus status, Pageable pageable);
}
