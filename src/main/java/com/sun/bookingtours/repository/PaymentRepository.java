package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Tìm payment theo bookingId — dùng cho GET /api/payments/{bookingId}
    // Spring Data tự sinh query từ tên method: findBy + BookingId
    Optional<Payment> findByBookingId(UUID bookingId);
}
