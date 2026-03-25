package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

    List<Food> findAllByIdIn(List<UUID> ids);
}
