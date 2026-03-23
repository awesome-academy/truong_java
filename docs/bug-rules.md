# Bug Rules — Những lỗi đã xảy ra, không được lặp lại

---

## Rule 1: POST endpoint không được truyền sensitive data qua @RequestParam

**Sai:**
```java
@PostMapping("/refresh")
public ResponseEntity<?> refresh(@RequestParam String refreshToken) { ... }

// Kết quả: POST /api/auth/refresh?refreshToken=eyJhbGci...
```

**Đúng:**
```java
@PostMapping("/refresh")
public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) { ... }
```

**Tại sao:**
- Token trên URL bị ghi vào server logs, proxy logs, browser history → lộ credential
- REST convention: POST/PUT/DELETE phải đưa data vào request body
- `@RequestParam` chỉ dùng cho GET với non-sensitive filter params (page, size, search...)

**Áp dụng:** Mọi endpoint POST/PUT/DELETE nhận token, password, hay bất kỳ sensitive data nào đều phải dùng `@RequestBody`.

---

## Rule 2: Không lưu refresh_token trực tiếp trong bảng users

**Sai:**
```sql
-- V2__add_refresh_token_to_users.sql
ALTER TABLE users ADD COLUMN refresh_token TEXT;
```
```java
// User.java
private String refreshToken; // ❌ không thuộc về profile table
```

**Đúng:**
```sql
CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

**Tại sao:**
- Bảng `users` chứa thông tin profile, không phải auth session (Single Responsibility)
- Không scale: 1 user nhiều device/session → không thể lưu nhiều token trong 1 column
- UPDATE `users` mỗi lần refresh → lock row không cần thiết
- Khó audit và revoke từng session riêng lẻ

**Áp dụng:** Bất kỳ thứ gì liên quan đến session/token đều phải bảng riêng, không nhét vào bảng entity chính.
