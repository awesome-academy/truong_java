package com.sun.bookingtours.service;

import com.sun.bookingtours.entity.RevokedAccessToken;
import com.sun.bookingtours.repository.RevokedAccessTokenRepository;
import com.sun.bookingtours.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevokedAccessTokenService {

    private final RevokedAccessTokenRepository repo;
    private final JwtTokenProvider jwtTokenProvider;

    // Gọi khi admin logout: lưu jti vào bảng để filter từ chối token này về sau
    @Transactional
    public void revoke(String token) {
        try {
            Claims claims = jwtTokenProvider.getClaims(token);
            String jti = claims.getId();
            if (jti == null || jti.isBlank()) {
                log.warn("Attempted to revoke a token without a valid jti; skipping revoke operation");
                return;
            }
            LocalDateTime expiresAt = claims.getExpiration().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            repo.save(new RevokedAccessToken(jti, expiresAt));
        } catch (JwtException e) {
            log.warn("Attempted to revoke an invalid token; skipping revoke operation: {}", e.getMessage());
        }
    }

    // Filter gọi method này sau khi verify chữ ký để check token có bị revoke không
    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        return repo.existsById(jti);
    }

    // Xóa record đã quá hạn để bảng không tích lũy vô hạn.
    // Token hết hạn rồi thì filter cũng tự reject bởi validateToken() — record không cần thiết nữa.
    @Scheduled(fixedRate = 3_600_000) // mỗi 1 giờ
    @Transactional
    public void cleanupExpired() {
        repo.deleteByExpiresAtBefore(LocalDateTime.now());
        log.debug("Cleaned up expired revoked access tokens");
    }
}
