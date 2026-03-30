-- Bảng lưu jti của access token đã bị revoke (khi admin logout)
-- jti (JWT ID) là UUID duy nhất được nhúng vào mỗi access token khi issue
-- Filter kiểm tra jti ở đây trước khi accept token → chặn reuse sau logout
-- expires_at dùng để cleanup: @Scheduled xóa record đã quá hạn mỗi giờ

CREATE TABLE revoked_access_tokens (
    jti        VARCHAR(36)  PRIMARY KEY,
    expires_at TIMESTAMP    NOT NULL
);

-- Index để cleanup query chạy nhanh: DELETE WHERE expires_at < NOW()
CREATE INDEX idx_revoked_access_tokens_expires ON revoked_access_tokens(expires_at);
