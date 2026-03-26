package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Review;
import com.sun.bookingtours.entity.enums.TargetType;
import org.springframework.data.jpa.domain.Specification;

public class ReviewSpecification {

    private ReviewSpecification() {}

    public static Specification<Review> isApproved(Boolean isApproved) {
        return (root, query, cb) ->
                isApproved == null ? null : cb.equal(root.get("isApproved"), isApproved);
    }

    public static Specification<Review> hasTargetType(TargetType targetType) {
        return (root, query, cb) ->
                targetType == null ? null : cb.equal(root.get("targetType"), targetType);
    }
}
