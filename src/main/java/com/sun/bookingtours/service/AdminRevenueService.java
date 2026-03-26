package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.response.MonthlyRevenueResponse;
import com.sun.bookingtours.dto.response.RevenueOverviewResponse;
import com.sun.bookingtours.dto.response.TourRevenueResponse;
import com.sun.bookingtours.entity.enums.BookingStatus;
import com.sun.bookingtours.entity.enums.PaymentStatus;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.repository.BookingRepository;
import com.sun.bookingtours.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminRevenueService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public RevenueOverviewResponse getOverview(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("fromDate không được sau toDate");
        }

        // atStartOfDay / atTime(MAX) → đảm bảo bao gồm toàn bộ ngày đầu và cuối
        var fromDt = from.atStartOfDay();
        var toDt   = to.atTime(LocalTime.MAX);

        BigDecimal totalRevenue = paymentRepository.sumByStatusAndPaidAtBetween(
                PaymentStatus.SUCCESS, fromDt, toDt);

        // LinkedHashMap → giữ thứ tự insert, enum values theo thứ tự khai báo
        Map<String, Long> bookingCountByStatus = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            bookingCountByStatus.put(
                    status.name(),
                    bookingRepository.countByStatusAndBookedAtBetween(status, fromDt, toDt)
            );
        }

        List<MonthlyRevenueResponse> revenueByMonth = paymentRepository.getMonthlyRevenue(
                PaymentStatus.SUCCESS, fromDt, toDt);

        return new RevenueOverviewResponse(totalRevenue, bookingCountByStatus, revenueByMonth);
    }

    @Transactional(readOnly = true)
    public List<TourRevenueResponse> getTopTours(LocalDate from, LocalDate to, int limit) {
        if (from.isAfter(to)) {
            throw new BusinessException("fromDate không được sau toDate");
        }

        if (limit < 1 || limit > 100) {
            throw new BusinessException("limit phải từ 1 đến 100");
        }

        return paymentRepository.getTopToursByRevenue(
                PaymentStatus.SUCCESS,
                from.atStartOfDay(),
                to.atTime(LocalTime.MAX),
                // PageRequest.of(0, limit) → lấy trang đầu tiên với size = limit → tương đương LIMIT n
                PageRequest.of(0, limit)
        );
    }
}
