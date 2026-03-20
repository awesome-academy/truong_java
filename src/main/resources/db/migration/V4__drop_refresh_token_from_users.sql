-- Xóa cột refresh_token khỏi bảng users (đã chuyển sang bảng refresh_tokens)
ALTER TABLE users DROP COLUMN IF EXISTS refresh_token;
