package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Tour;
import com.sun.bookingtours.entity.enums.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
// JpaSpecificationExecutor — cho phép dùng Specification để build dynamic query (filter tùy chọn)
public interface TourRepository extends JpaRepository<Tour, UUID>, JpaSpecificationExecutor<Tour> {

    boolean existsBySlug(String slug);

    // Chỉ JOIN FETCH images (OneToMany) — tránh N+1 cho collection lớn nhất
    // places và foods (ManyToMany List) được lazy-load trong transaction: không cần fetch ở đây
    // vì JOIN FETCH đồng thời >= 2 bag (unordered List) gây MultipleBagFetchException
    @Query("""
        SELECT t FROM Tour t
        LEFT JOIN FETCH t.images
        WHERE t.slug = :slug AND t.deletedAt IS NULL
    """)
    Optional<Tour> findBySlugWithDetails(@Param("slug") String slug);

    // Tìm theo id — dùng cho admin (không cần filter deleted)
    @Query("""
        SELECT t FROM Tour t
        LEFT JOIN FETCH t.images
        WHERE t.id = :id
    """)
    Optional<Tour> findByIdWithDetails(@Param("id") UUID id);

    // Public list: chỉ ACTIVE, chưa bị xóa, có pagination
    Page<Tour> findByStatusAndDeletedAtIsNull(TourStatus status, Pageable pageable);

    // Search: ILIKE = case-insensitive LIKE của PostgreSQL
    // Tìm trong title, description, departure_location
    @Query("""
        SELECT t FROM Tour t
        WHERE t.status = 'ACTIVE' AND t.deletedAt IS NULL
        AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(t.departureLocation) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Page<Tour> search(@Param("q") String q, Pageable pageable);
}
