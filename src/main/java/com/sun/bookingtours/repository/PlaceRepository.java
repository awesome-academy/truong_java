package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaceRepository extends JpaRepository<Place, UUID> {

    List<Place> findAllByIdIn(List<UUID> ids);
}
