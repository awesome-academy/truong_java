# CLAUDE.md

## Bug Rules

### 1. POST endpoint không dùng @RequestParam cho sensitive data

Token/credential phải đi trong request body, không phải URL. `@RequestParam` chỉ dùng cho GET với non-sensitive params (page, size, search...).

### 2. Race condition khi update trạng thái độc quyền

Khi update field mà chỉ 1 row được phép có giá trị đó (vd: `is_default`, `is_primary`...), phải xử lý 2 tầng:
- **Service**: dùng bulk UPDATE (`@Modifying @Query`) để unset tất cả trước, không dùng read-then-update
- **DB**: thêm partial unique index làm safety net, vd: `CREATE UNIQUE INDEX ON table (user_id) WHERE is_default = true`
