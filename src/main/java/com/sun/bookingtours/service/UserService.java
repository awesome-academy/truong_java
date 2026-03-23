package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.ChangePasswordRequest;
import com.sun.bookingtours.dto.request.UpdateProfileRequest;
import com.sun.bookingtours.dto.response.UserProfileResponse;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.UserMapper;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // Spring inject UserMapperImpl (class MapStruct tự generate lúc compile)
    private final UserMapper userMapper;

    public UserProfileResponse getProfile(UserPrincipal principal) {
        User user = findUserById(principal.getId());
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request) {
        User user = findUserById(principal.getId());

        // Partial update: chỉ cập nhật field nào client gửi lên (khác null)
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        // Không cần save() — JPA dirty checking tự UPDATE khi transaction kết thúc
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        User user = findUserById(principal.getId());

        // matches(rawPassword, encodedPassword): so sánh plain-text với hash, không decode ngược
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu cũ không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
