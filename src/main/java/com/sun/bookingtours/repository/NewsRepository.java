package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {

    Page<News> findAllByIsPublishedTrue(Pageable pageable);

    Optional<News> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
