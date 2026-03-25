package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.CancelBookingRequest;
import com.sun.bookingtours.dto.request.CreateBookingRequest;
import com.sun.bookingtours.dto.response.BookingResponse;
import com.sun.bookingtours.entity.Booking;
import com.sun.bookingtours.entity.TourSchedule;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.entity.enums.ActivityType;
import com.sun.bookingtours.entity.enums.BookingStatus;
import com.sun.bookingtours.entity.enums.ScheduleStatus;
import com.sun.bookingtours.event.ActivityLogEvent;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.BookingMapper;
import com.sun.bookingtours.repository.BookingRepository;
import com.sun.bookingtours.repository.TourScheduleRepository;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TourScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookingResponse createBooking(UserPrincipal principal, CreateBookingRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));

        // findByIdWithLock → PESSIMISTIC_WRITE lock để tránh race condition overbooking
        TourSchedule schedule = scheduleRepository.findByIdWithLock(request.scheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("TourSchedule", request.scheduleId()));

        if (schedule.getStatus() != ScheduleStatus.OPEN) {
            throw new BusinessException("Lịch khởi hành này không còn nhận đặt chỗ");
        }

        // List.of() → immutable list, truyền vào IN clause của JPQL
        // COALESCE trong query đảm bảo trả 0 thay vì null khi chưa có booking
        int usedSlots = bookingRepository.sumParticipantsByScheduleAndStatus(
                schedule.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        int availableSlots = schedule.getTotalSlots() - usedSlots;
        if (availableSlots < request.numParticipants()) {
            throw new BusinessException("Không đủ chỗ. Còn lại: " + availableSlots + " chỗ");
        }

        // schedule.getTour() trigger lazy load — an toàn vì đang trong @Transactional
        BigDecimal unitPrice = schedule.getPriceOverride() != null
                ? schedule.getPriceOverride()
                : schedule.getTour().getBasePrice();

        // BigDecimal.valueOf(int) → tránh lỗi làm tròn của double (0.1 + 0.2 ≠ 0.3)
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(request.numParticipants()));

        Booking booking = Booking.builder()
                .user(user)
                .schedule(schedule)
                .bookingCode(generateBookingCode())
                .numParticipants(request.numParticipants())
                .totalAmount(totalAmount)
                .build();

        Booking saved = bookingRepository.save(booking);

        eventPublisher.publishEvent(new ActivityLogEvent(user, saved, ActivityType.BOOKING_CREATED));

        return bookingMapper.toResponse(saved);
    }

    // readOnly = true → Hibernate bỏ dirty checking → tiết kiệm memory
    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(UserPrincipal principal, BookingStatus status, Pageable pageable) {
        // Page<T>.map() → convert từng element, giữ nguyên metadata (totalElements, totalPages...)
        return bookingRepository.findByUserIdAndStatus(principal.getId(), status, pageable)
                .map(bookingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse getMyBookingById(UserPrincipal principal, UUID bookingId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(UserPrincipal principal, UUID bookingId, CancelBookingRequest request) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Không thể huỷ booking ở trạng thái: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason(request.cancelReason());
        // Không cần save() — dirty checking tự sinh UPDATE khi transaction commit

        eventPublisher.publishEvent(new ActivityLogEvent(booking.getUser(), booking, ActivityType.BOOKING_CANCELLED));

        return bookingMapper.toResponse(booking);
    }

    // ---- private helpers ----

    private String generateBookingCode() {
        // DateTimeFormatter.ofPattern() → format ngày theo pattern tuỳ chỉnh
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase()
        // → lấy 8 hex chars ngẫu nhiên, viết hoa → VD: "A1B2C3D4"
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        return "TOUR-" + date + "-" + unique;
    }
}
