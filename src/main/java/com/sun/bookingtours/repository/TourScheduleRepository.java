package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.TourSchedule;
import com.sun.bookingtours.entity.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TourScheduleRepository extends JpaRepository<TourSchedule, UUID> {
    List<TourSchedule> findByTourId(UUID tourId);
    List<TourSchedule> findByTourIdAndStatus(UUID tourId, ScheduleStatus status);
}
