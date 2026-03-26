package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.response.AdminUserDetailResponse;
import com.sun.bookingtours.dto.response.AdminUserResponse;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.entity.enums.Role;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.BookingMapper;
import com.sun.bookingtours.repository.BookingRepository;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listUsers(Role role, Boolean isActive, String search, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasRole(role))
                .and(UserSpecification.isActive(isActive))
                .and(UserSpecification.search(search));

        return userRepository.findAll(spec, pageable).map(this::toAdminUserResponse);
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .recentBookings(bookingRepository.findTop5ByUserIdOrderByBookedAtDesc(userId)
                        .stream().map(bookingMapper::toResponse).toList())
                .build();
    }

    @Transactional
    public AdminUserResponse activate(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setActive(true);
        return toAdminUserResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse deactivate(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setActive(false);
        return toAdminUserResponse(userRepository.save(user));
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
