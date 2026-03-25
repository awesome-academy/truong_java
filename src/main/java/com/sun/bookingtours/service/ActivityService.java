package com.sun.bookingtours.service;

import com.sun.bookingtours.entity.Activity;
import com.sun.bookingtours.event.ActivityLogEvent;
import com.sun.bookingtours.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
}
