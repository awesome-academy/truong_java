package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.response.ActivityResponse;
import com.sun.bookingtours.entity.Activity;
import com.sun.bookingtours.entity.enums.ActivityType;
import com.sun.bookingtours.event.ActivityLogEvent;
import com.sun.bookingtours.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    // AFTER_COMMIT → chạy sau khi transaction cha commit xong → booking đã tồn tại trên DB → FK thoả
    // REQUIRES_NEW → transaction riêng, độc lập hoàn toàn với caller
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(ActivityLogEvent event) {
        Activity activity = Activity.builder()
                .user(event.user())
                .booking(event.booking())
                .type(event.type())
                .build();
        activityRepository.save(activity);
    }

    public Page<ActivityResponse> getMyActivities(UUID userId, Pageable pageable) {
        return activityRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    public Page<ActivityResponse> getAdminActivities(UUID userId, ActivityType type, Pageable pageable) {
        // Specification: build điều kiện WHERE động — chỉ add vào nếu param != null
        Specification<Activity> spec = Specification.where((Specification<Activity>) null);

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }
        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }

        return activityRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private ActivityResponse toResponse(Activity a) {
        return ActivityResponse.builder()
                .id(a.getId())
                .userId(a.getUser().getId())
                .userEmail(a.getUser().getEmail())
                .bookingId(a.getBooking() != null ? a.getBooking().getId() : null)
                .bookingCode(a.getBooking() != null ? a.getBooking().getBookingCode() : null)
                .type(a.getType())
                .metadata(a.getMetadata())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
