package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.TourSchedule;
import com.sun.bookingtours.entity.enums.ScheduleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TourScheduleRepository extends JpaRepository<TourSchedule, UUID> {
    List<TourSchedule> findByTourId(UUID tourId);
    List<TourSchedule> findByTourIdAndStatus(UUID tourId, ScheduleStatus status);

    // PESSIMISTIC_WRITE → lock row trong DB cho đến khi transaction commit
    // Đảm bảo chỉ 1 transaction được kiểm tra slot + save booking cùng lúc
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TourSchedule s WHERE s.id = :id")
    Optional<TourSchedule> findByIdWithLock(@Param("id") UUID id);
}
