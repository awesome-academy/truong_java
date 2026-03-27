package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

    List<Food> findAllByIdIn(List<UUID> ids);

    Optional<Food> findBySlug(String slug);

    List<Food> findAllByIsActiveTrue();

    boolean existsBySlug(String slug);
}
