package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Tour;
import com.sun.bookingtours.entity.enums.TourStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class TourSpecification {

    // Chỉ trả tour ACTIVE và chưa bị soft-delete — luôn áp dụng cho public API
    public static Specification<Tour> isPublic() {
        return (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("status"), TourStatus.ACTIVE),
                        cb.isNull(root.get("deletedAt"))
                );
    }

    // Nếu param null → trả (root, query, cb) -> cb.conjunction() (no-op) — Specification.and() không chấp nhận null Java
    public static Specification<Tour> hasCategory(UUID categoryId) {
        if (categoryId == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Tour> minPrice(BigDecimal min) {
        if (min == null) return (root, query, cb) -> cb.conjunction();
        // greaterThanOrEqualTo = SQL: base_price >= min
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("basePrice"), min);
    }

    public static Specification<Tour> maxPrice(BigDecimal max) {
        if (max == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("basePrice"), max);
    }

    public static Specification<Tour> hasDuration(Integer days) {
        if (days == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("durationDays"), days);
    }

    public static Specification<Tour> hasDeparture(String location) {
        if (location == null || location.isBlank()) return (root, query, cb) -> cb.conjunction();
        // like() = SQL: LOWER(departure_location) LIKE LOWER('%location%')
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("departureLocation")),
                        "%" + location.toLowerCase() + "%");
    }
}
