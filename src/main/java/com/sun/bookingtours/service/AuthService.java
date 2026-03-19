package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.LoginRequest;
import com.sun.bookingtours.dto.request.RegisterRequest;
import com.sun.bookingtours.dto.response.AuthResponse;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.security.JwtTokenProvider;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use");
        }

        // Tạo user mới, password được hash bằng BCrypt trước khi lưu
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        // Tạo token ngay sau khi register, user không cần login lại
        return generateTokens(user.getEmail());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        /*
         * AuthenticationManager.authenticate() sẽ:
         * 1. Gọi UserDetailsServiceImpl.loadUserByUsername(email) để load user từ DB
         * 2. So sánh password với passwordHash bằng BCrypt
         * 3. Throw BadCredentialsException nếu sai → Spring tự trả 401
         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokens(userPrincipal.getEmail());
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        // Validate chữ ký và hạn của refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // Kiểm tra refresh token có khớp với cái đang lưu trong DB không
        // Nếu user đã logout (refreshToken = null) hoặc token bị đổi → reject
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new BusinessException("Refresh token mismatch");
        }

        return generateTokens(email);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        // Xóa refresh token trong DB → token cũ không thể dùng để refresh nữa
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    /*
     * Helper: tạo cặp accessToken + refreshToken, lưu refreshToken vào DB
     * Tách ra method riêng vì register, login, refresh đều dùng chung logic này
     */
    private AuthResponse generateTokens(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        // Tạo UserPrincipal để generate access token (cần role, id...)
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getRole().name(), user.isActive()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        // Lưu refresh token vào DB để có thể invalidate khi logout
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
