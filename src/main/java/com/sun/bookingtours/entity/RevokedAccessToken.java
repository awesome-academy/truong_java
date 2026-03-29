package com.sun.bookingtours.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "revoked_access_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RevokedAccessToken {

    // jti là UUID string từ JWT claim "jti" — dùng làm PK để lookup O(1)
    @Id
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
