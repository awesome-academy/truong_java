package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.AdminCancelBookingRequest;
import com.sun.bookingtours.dto.response.AdminBookingDetailResponse;
import com.sun.bookingtours.dto.response.AdminBookingResponse;
import com.sun.bookingtours.dto.response.PaymentResponse;
import com.sun.bookingtours.entity.Booking;
import com.sun.bookingtours.entity.enums.BookingStatus;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.AdminBookingMapper;
import com.sun.bookingtours.mapper.PaymentMapper;
import com.sun.bookingtours.repository.BookingRepository;
import com.sun.bookingtours.repository.BookingSpecification;
import com.sun.bookingtours.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final AdminBookingMapper adminBookingMapper;

    @Transactional(readOnly = true)
    public Page<AdminBookingResponse> listBookings(
            BookingStatus status, LocalDate fromDate, LocalDate toDate, UUID userId, Pageable pageable) {

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException("fromDate không được sau toDate");
        }

        Specification<Booking> spec = Specification
                .where(BookingSpecification.hasStatus(status))
                .and(BookingSpecification.hasUserId(userId))
                .and(BookingSpecification.fromDate(fromDate))
                .and(BookingSpecification.toDate(toDate));

        return bookingRepository.findAll(spec, pageable).map(adminBookingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminBookingDetailResponse getBookingDetail(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        // Payment là unidirectional OneToOne (Payment→Booking), phía Booking không có field payment
        // → phải query riêng từ PaymentRepository
        PaymentResponse paymentResponse = paymentRepository.findByBookingId(bookingId)
                .map(paymentMapper::toResponse)
                .orElse(null);

        return adminBookingMapper.toDetailResponse(booking, paymentResponse);
    }

    @Transactional
    public AdminBookingResponse completeBooking(UUID bookingId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Chỉ có thể hoàn thành booking ở trạng thái CONFIRMED, hiện tại: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return adminBookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public AdminBookingResponse cancelBooking(UUID bookingId, AdminCancelBookingRequest request) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Không thể hủy booking ở trạng thái: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason(request.cancelReason());

        return adminBookingMapper.toResponse(bookingRepository.save(booking));
    }

}
