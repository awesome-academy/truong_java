package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.entity.enums.Role;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasRole(Role role) {
        if (role == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> isActive(Boolean active) {
        if (active == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("isActive"), active);
    }

    // Tìm kiếm trong fullName hoặc email — OR giữa 2 column, case-insensitive
    public static Specification<User> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return (root, query, cb) -> cb.conjunction();
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("fullName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern)
        );
    }
}
