# CLAUDE.md

## Workflow Rules

### 1. Làm task theo từng bước — KHÔNG làm all-in

Trước khi code, phải:
1. **Thảo luận** với user về các bước sẽ làm (liệt kê rõ bước 1, bước 2...)
2. **Giải thích** keyword mới hoặc challenge kỹ thuật trước khi code
3. **Chờ xác nhận** rồi mới làm từng bước một
4. Những chỗ code tường minh rồi thì không cần comment. Chỉ comment với những chỗ logic phức tạp hoặc keyword mới
5. Sau mỗi bước, hỏi user muốn tiếp tục không
6. Cần đưa ra các solution như một dự án thực tế

---

## Bug Rules

### 1. POST endpoint không dùng @RequestParam cho sensitive data

Token/credential phải đi trong request body, không phải URL. `@RequestParam` chỉ dùng cho GET với non-sensitive params (page, size, search...).

### 2. Race condition khi update trạng thái độc quyền

Khi update field mà chỉ 1 row được phép có giá trị đó (vd: `is_default`, `is_primary`...), phải xử lý 2 tầng:
- **Service**: dùng bulk UPDATE (`@Modifying @Query`) để unset tất cả trước, không dùng read-then-update
- **DB**: thêm partial unique index làm safety net, vd: `CREATE UNIQUE INDEX ON table (user_id) WHERE is_default = true`

