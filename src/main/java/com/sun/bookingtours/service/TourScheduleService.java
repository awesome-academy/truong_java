package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.TourScheduleRequest;
import com.sun.bookingtours.dto.request.TourScheduleStatusRequest;
import com.sun.bookingtours.dto.response.TourScheduleResponse;
import com.sun.bookingtours.entity.Tour;
import com.sun.bookingtours.entity.TourSchedule;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.repository.TourRepository;
import com.sun.bookingtours.repository.TourScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TourScheduleService {

    private final TourScheduleRepository scheduleRepository;
    private final TourRepository tourRepository;

    // readOnly = true — Hibernate bỏ dirty checking, tiết kiệm memory
    @Transactional(readOnly = true)
    public List<TourScheduleResponse> listByTour(UUID tourId) {
        validateTourExists(tourId);
        return scheduleRepository.findByTourId(tourId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TourScheduleResponse create(UUID tourId, TourScheduleRequest request) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));

        TourSchedule schedule = TourSchedule.builder()
                .tour(tour)
                .departureDate(request.departureDate())  // record getter — không có prefix "get"
                .returnDate(request.returnDate())
                .totalSlots(request.totalSlots())
                .priceOverride(request.priceOverride())
                .build();

        return toResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public TourScheduleResponse update(UUID scheduleId, TourScheduleRequest request) {
        TourSchedule schedule = findById(scheduleId);

        schedule.setDepartureDate(request.departureDate());
        schedule.setReturnDate(request.returnDate());
        schedule.setTotalSlots(request.totalSlots());
        schedule.setPriceOverride(request.priceOverride());

        // Không cần save() — dirty checking tự sinh UPDATE khi transaction commit
        return toResponse(schedule);
    }

    @Transactional
    public TourScheduleResponse updateStatus(UUID scheduleId, TourScheduleStatusRequest request) {
        TourSchedule schedule = findById(scheduleId);
        schedule.setStatus(request.status());
        return toResponse(schedule);
    }

    // ---- private helpers ----

    private TourSchedule findById(UUID id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TourSchedule", id));
    }

    private void validateTourExists(UUID tourId) {
        if (!tourRepository.existsById(tourId)) {
            throw new ResourceNotFoundException("Tour", tourId);
        }
    }

    private TourScheduleResponse toResponse(TourSchedule s) {
        return new TourScheduleResponse(
                s.getId(),
                s.getDepartureDate(),
                s.getReturnDate(),
                s.getTotalSlots(),
                s.getPriceOverride(),
                s.getStatus()
        );
    }
}
