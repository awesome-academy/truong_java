package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Booking;
import com.sun.bookingtours.entity.enums.BookingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class BookingSpecification {

    private BookingSpecification() {}

    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> hasUserId(UUID userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    // bookedAt >= fromDate (00:00:00 của ngày đó)
    public static Specification<Booking> fromDate(LocalDate fromDate) {
        return (root, query, cb) ->
                fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("bookedAt"), fromDate.atStartOfDay());
    }

    // bookedAt <= toDate (23:59:59 của ngày đó)
    public static Specification<Booking> toDate(LocalDate toDate) {
        return (root, query, cb) ->
                toDate == null ? null : cb.lessThanOrEqualTo(root.get("bookedAt"), toDate.atTime(LocalTime.MAX));
    }
}
