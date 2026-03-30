package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.RevokedAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, String> {

    // Dùng cho @Scheduled cleanup: xóa tất cả record đã quá expires_at
    void deleteByExpiresAtBefore(LocalDateTime threshold);
}
