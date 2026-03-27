package com.sun.bookingtours.repository;

import com.sun.bookingtours.dto.response.MonthlyRevenueResponse;
import com.sun.bookingtours.dto.response.TourRevenueResponse;
import com.sun.bookingtours.entity.Payment;
import com.sun.bookingtours.entity.enums.PaymentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByBookingId(UUID bookingId);

    // COALESCE → trả 0 thay vì null khi chưa có payment nào trong khoảng thời gian
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.status = :status
              AND p.paidAt BETWEEN :from AND :to
            """)
    BigDecimal sumByStatusAndPaidAtBetween(
            @Param("status") PaymentStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Constructor Expression: SELECT new FullClassName(field1, field2, ...)
    // Hibernate gọi constructor của MonthlyRevenueResponse(int year, int month, BigDecimal revenue)
    // YEAR() / MONTH() là JPQL functions, Hibernate dịch sang SQL tương ứng của từng DB
    @Query("""
            SELECT new com.sun.bookingtours.dto.response.MonthlyRevenueResponse(
                YEAR(p.paidAt), MONTH(p.paidAt), SUM(p.amount)
            )
            FROM Payment p
            WHERE p.status = :status
              AND p.paidAt BETWEEN :from AND :to
            GROUP BY YEAR(p.paidAt), MONTH(p.paidAt)
            ORDER BY YEAR(p.paidAt) ASC, MONTH(p.paidAt) ASC
            """)
    List<MonthlyRevenueResponse> getMonthlyRevenue(
            @Param("status") PaymentStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // JOIN chain: Payment → booking (Booking) → schedule (TourSchedule) → tour (Tour)
    // Pageable ở cuối → Spring Data tự thêm LIMIT/OFFSET vào native query
    @Query("""
            SELECT new com.sun.bookingtours.dto.response.TourRevenueResponse(
                t.id, t.title, t.slug, SUM(p.amount), COUNT(p)
            )
            FROM Payment p
            JOIN p.booking b
            JOIN b.schedule s
            JOIN s.tour t
            WHERE p.status = :status
              AND p.paidAt BETWEEN :from AND :to
            GROUP BY t.id, t.title, t.slug
            ORDER BY SUM(p.amount) DESC
            """)
    List<TourRevenueResponse> getTopToursByRevenue(
            @Param("status") PaymentStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
