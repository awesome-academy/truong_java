package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.CreatePaymentRequest;
import com.sun.bookingtours.dto.response.PaymentResponse;
import com.sun.bookingtours.entity.Booking;
import com.sun.bookingtours.entity.Payment;
import com.sun.bookingtours.entity.UserBankAccount;
import com.sun.bookingtours.entity.enums.ActivityType;
import com.sun.bookingtours.entity.enums.BookingStatus;
import com.sun.bookingtours.entity.enums.PaymentMethod;
import com.sun.bookingtours.entity.enums.PaymentStatus;
import com.sun.bookingtours.event.ActivityLogEvent;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.PaymentMapper;
import com.sun.bookingtours.repository.BookingRepository;
import com.sun.bookingtours.repository.PaymentRepository;
import com.sun.bookingtours.repository.UserBankAccountRepository;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserBankAccountRepository bankAccountRepository;
    private final PaymentMapper paymentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PaymentResponse createPayment(UserPrincipal principal, CreatePaymentRequest request) {
        Booking booking = bookingRepository.findByIdAndUserIdForUpdate(request.bookingId(), principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.bookingId()));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Chỉ có thể thanh toán booking ở trạng thái PENDING");
        }

        // Mock project — thực tế sẽ gọi API VNPay / Momo / ... ở đây
        if (request.method() != PaymentMethod.INTERNET_BANKING) {
            throw new BusinessException("Chỉ hỗ trợ phương thức INTERNET_BANKING");
        }

        // findByIdAndUserId → đảm bảo bank account thuộc đúng user đang đăng nhập
        UserBankAccount bankAccount = null;
        if (request.bankAccountId() != null) {
            bankAccount = bankAccountRepository.findByIdAndUserId(request.bankAccountId(), principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("BankAccount", request.bankAccountId()));
        }

        // Mock: lưu payment với status=SUCCESS và paidAt=now() luôn
        // Thực tế: status=PENDING, chờ callback từ cổng thanh toán rồi mới update
        Payment payment = Payment.builder()
                .booking(booking)
                .bankAccount(bankAccount)
                .amount(booking.getTotalAmount())
                .method(request.method())
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CONFIRMED);
        // Không cần bookingRepository.save() — dirty checking tự UPDATE khi transaction commit

        eventPublisher.publishEvent(new ActivityLogEvent(booking.getUser(), booking, ActivityType.PAYMENT_COMPLETED));

        return paymentMapper.toResponse(payment);
    }

    // readOnly = true → Hibernate bỏ dirty checking → tiết kiệm memory
    @Transactional(readOnly = true)
    public PaymentResponse getByBookingId(UserPrincipal principal, UUID bookingId) {
        bookingRepository.findByIdAndUserId(bookingId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        return paymentRepository.findByBookingId(bookingId)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for bookingId", bookingId));
    }
}
