package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.LoginRequest;
import com.sun.bookingtours.dto.request.RegisterRequest;
import com.sun.bookingtours.dto.response.AuthResponse;
import com.sun.bookingtours.entity.RefreshToken;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.repository.RefreshTokenRepository;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.security.JwtTokenProvider;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        return generateTokens(user);
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
        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", userPrincipal.getEmail()));

        return generateTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String tokenValue) {
        // Validate chữ ký và hạn của refresh token
        if (!jwtTokenProvider.validateToken(tokenValue)) {
            throw new BusinessException("Invalid or expired refresh token");
        }

        // Kiểm tra token có tồn tại trong DB không (chưa bị logout/revoke)
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("Refresh token not found or already revoked"));

        // Xóa token cũ và cấp token mới (rotation)
        refreshTokenRepository.delete(refreshToken);

        return generateTokens(refreshToken.getUser());
    }

    @Transactional
    public void logout(String tokenValue) {
        if (!jwtTokenProvider.validateToken(tokenValue)) {
            throw new BusinessException("Invalid refresh token");
        }

        // Xóa token khỏi DB → token không thể dùng để refresh nữa
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("Refresh token not found"));

        refreshTokenRepository.delete(refreshToken);
    }

    /*
     * Helper: tạo cặp accessToken + refreshToken, lưu refreshToken vào bảng refresh_tokens
     * Tách ra method riêng vì register, login, refresh đều dùng chung logic này
     */
    private AuthResponse generateTokens(User user) {
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getRole().name(), user.isActive()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String tokenValue = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // Lưu refresh token vào bảng riêng để có thể invalidate khi logout
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(tokenValue)
                .build();
    }
}
