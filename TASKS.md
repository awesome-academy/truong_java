# SUN Booking Tours — Task Checklist

> Stack: Spring Boot · Spring Data JPA · Spring Security · JWT · OAuth2 · PostgreSQL
> Roles: ADMIN (API + UI) · USER (API) · GUEST (API, anonymous)

---

## PHASE 1 — Project Foundation

### T01 — Project Setup
- [x] Tạo project qua Spring Initializr (xem hướng dẫn bên dưới)
- [x] Cấu hình `application.properties`: datasource, JPA, timezone
- [x] Tạo package structure:
  ```
  com.sun.booking
  ├── config/
  ├── controller/
  ├── service/
  ├── repository/
  ├── entity/
  ├── dto/
  │   ├── request/
  │   └── response/
  ├── mapper/
  ├── security/
  ├── exception/
  └── util/
  ```
- [x] Tạo `ApiResponse<T>` wrapper cho tất cả response
- [x] Tạo `GlobalExceptionHandler` (`@RestControllerAdvice`)
- [x] Tạo các custom exception: `ResourceNotFoundException`, `BusinessException`, `UnauthorizedException`
- [x] Chạy `schema.sql` lên PostgreSQL (Flyway migration)
- [x] Verify kết nối DB thành công khi start app

---

## PHASE 2 — Auth & User

### T02 — JWT Authentication
- [x] Thêm dependency `jjwt` vào `pom.xml`
- [x] Tạo `JwtTokenProvider`: generate, validate, parse token
- [x] Tạo `JwtAuthenticationFilter` (extends `OncePerRequestFilter`)
- [x] Cấu hình `SecurityFilterChain`:
  - Public routes: `/api/auth/**`, `GET /api/tours/**`, `GET /api/places/**`, `GET /api/foods/**`, `GET /api/news/**`, `GET /api/reviews/**`, `GET /api/categories/**`
  - USER routes: `/api/bookings/**`, `/api/payments/**`, `/api/reviews/**` (POST/PUT/DELETE), `/api/comments/**`, `/api/users/me/**`
  - ADMIN routes: `/api/admin/**`
- [x] `UserDetailsService` load user by email từ DB
- [x] Tạo `UserPrincipal` (implements `UserDetails`)
- [x] `POST /api/auth/register` — tạo user mới (role=USER, is_active=true)
- [x] `POST /api/auth/login` — xác thực, trả `accessToken` + `refreshToken`
- [x] `POST /api/auth/refresh` — validate refresh token, cấp access token mới
- [x] `POST /api/auth/logout` — invalidate refresh token

### T03 — OAuth2 Login
- [ ] Thêm dependency `spring-boot-starter-oauth2-client`
- [ ] Cấu hình provider Google, Facebook, Twitter trong `application.yml`
- [ ] Implement `OAuth2UserService` (custom, load/create user từ OAuth profile)
- [ ] `OAuth2AuthenticationSuccessHandler`: sau login thành công → tạo JWT → redirect hoặc trả token
- [ ] Lưu vào bảng `oauth_accounts` (upsert theo provider + provider_user_id)
- [ ] Test flow: `GET /oauth2/authorize/{provider}` → callback → JWT

### T04 — User Profile
- [x] `GET /api/users/me` — trả thông tin profile của user đang đăng nhập
- [x] `PUT /api/users/me` — cập nhật `full_name`, `phone`, `avatar_url`
- [x] `PUT /api/users/me/password` — đổi mật khẩu (validate old password trước)

### T05 — Bank Account
- [x] `GET /api/users/me/bank-accounts` — list tài khoản ngân hàng
- [x] `POST /api/users/me/bank-accounts` — thêm mới
- [x] `PUT /api/users/me/bank-accounts/{id}` — cập nhật
- [x] `DELETE /api/users/me/bank-accounts/{id}` — xóa (chỉ được xóa của mình)
- [x] `PATCH /api/users/me/bank-accounts/{id}/default` — đặt làm mặc định (unset cái cũ)

---

## PHASE 3 — Category & Tour

### T06 — Category (Admin)
- [x] Entity `Category` với self-reference `parent_id`
- [x] `GET /api/categories` — list dạng tree (Guest + User)
- [x] `POST /api/admin/categories` — tạo category
- [x] `PUT /api/admin/categories/{id}` — cập nhật
- [x] `DELETE /api/admin/categories/{id}` — xóa / deactivate (`is_active=false`)

### T07 — Tour CRUD (Admin)
- [x] Entity `Tour`, `TourImage`, `TourPlace`, `TourFood`
- [x] `POST /api/admin/tours` — tạo tour (status mặc định `DRAFT`)
- [x] `PUT /api/admin/tours/{id}` — cập nhật thông tin tour
- [x] `DELETE /api/admin/tours/{id}` — soft delete (`deleted_at = NOW()`)
- [x] `PATCH /api/admin/tours/{id}/status` — chuyển status (`DRAFT`→`ACTIVE`→`INACTIVE`)
- [x] `POST /api/admin/tours/{id}/images` — upload/thêm ảnh
- [x] `DELETE /api/admin/tours/{id}/images/{imageId}` — xóa ảnh
- [x] `PUT /api/admin/tours/{id}/places` — set danh sách places liên kết
- [x] `PUT /api/admin/tours/{id}/foods` — set danh sách foods liên kết

### T08 — Tour Schedule (Admin)
- [x] Entity `TourSchedule`
- [x] `GET /api/admin/tours/{id}/schedules` — list schedules của tour
- [x] `POST /api/admin/tours/{id}/schedules` — tạo lịch khởi hành
- [x] `PUT /api/admin/schedules/{id}` — cập nhật lịch
- [x] `PATCH /api/admin/schedules/{id}/status` — đổi status (`OPEN`/`FULL`/`CANCELLED`)

### T09 — Tour Public API (Guest + User)
- [x] `GET /api/tours` — list tours (chỉ `status=ACTIVE`, `deleted_at IS NULL`)
  - Filter: `categoryId`, `minPrice`, `maxPrice`, `durationDays`, `departureLocation`
  - Pagination: `page`, `size`, `sort`
- [x] `GET /api/tours/{slug}` — chi tiết tour (kèm images, schedules còn OPEN, places, foods)
- [x] `GET /api/tours/search?q=` — tìm kiếm theo title, description, departure_location (LIKE hoặc full-text)

---

## PHASE 4 — Booking & Payment

### T10 — Booking (User)
- [x] Entity `Booking`
- [x] `POST /api/bookings` — tạo booking
  - [x] Validate schedule `status=OPEN`
  - [x] Validate còn đủ slot: `total_slots - SUM(num_participants của bookings CONFIRMED/PENDING) >= num_participants`
  - [x] Tính `total_amount = num_participants × (price_override ?? tour.base_price)`
  - [x] Sinh `booking_code` duy nhất (VD: `TOUR-YYYYMMDD-XXXXX`)
  - [x] Lưu booking `status=PENDING`
  - [x] Gọi `ActivityService.log(BOOKING_CREATED)`
- [x] `GET /api/bookings` — danh sách booking của user (filter `status`, pagination)
- [x] `GET /api/bookings/{id}` — chi tiết booking (chỉ của user đang đăng nhập)
- [x] `PATCH /api/bookings/{id}/cancel` — hủy booking
  - [x] Validate booking thuộc user, status phải là `PENDING` hoặc `CONFIRMED`
  - [x] Set `status=CANCELLED`, `cancelled_at`, `cancel_reason`
  - [x] Gọi `ActivityService.log(BOOKING_CANCELLED)`

### T11 — Payment (User)
- [x] Entity `Payment`
- [x] `POST /api/payments` — tạo payment
  - [x] Validate booking thuộc user, booking `status=PENDING`
  - [x] Validate `method=INTERNET_BANKING`
  - [x] Validate `bank_account_id` tồn tại và thuộc user (nếu truyền lên)
  - [x] Lưu payment `status=SUCCESS` (mock)
  - [x] Update booking `status=CONFIRMED`
  - [x] Gọi `ActivityService.log(PAYMENT_COMPLETED)`
- [x] `GET /api/payments/{bookingId}` — xem thông tin payment của booking

---

## PHASE 5 — Review, Rating & Social

### T12 — Review (User)
- [x] Entity `Review`
- [x] `POST /api/reviews` — tạo review
  - [x] Validate `target_type` ∈ {TOUR, PLACE, FOOD}
  - [x] Nếu `target_type=TOUR`: validate user có booking `status=COMPLETED` cho tour đó
  - [x] Mặc định `is_approved=false`
  - [x] Gọi `ActivityService.log(REVIEW_CREATED)`
- [x] `PUT /api/reviews/{id}` — cập nhật review (chỉ của mình, chưa được duyệt)
- [x] `DELETE /api/reviews/{id}` — xóa review của mình
- [x] `GET /api/reviews/me` — list review của user đang đăng nhập

### T13 — Review Public (Guest + User)
- [x] `GET /api/reviews?targetType=&targetId=` — list reviews `is_approved=true`, pagination
- [x] `GET /api/places/{slug}/reviews` — reviews của place cụ thể
- [x] `GET /api/foods/{slug}/reviews` — reviews của food cụ thể
- [x] `GET /api/tours/{slug}/reviews` — reviews của tour cụ thể

### T14 — Tour Rating (User)
- [x] Entity `TourRating`
- [x] `POST /api/tours/{id}/ratings` — rating tour (1–5)
  - [x] Validate user có booking `status=COMPLETED` cho tour
  - [x] Upsert (1 user chỉ rating 1 lần / tour)
  - [x] Trigger DB tự động cập nhật `tours.avg_rating`
- [x] `GET /api/tours/{id}/ratings/me` — xem rating của mình cho tour

### T15 — Review Likes (User)
- [x] Entity `ReviewLike`
- [x] `POST /api/reviews/{id}/like` — like (tạo record, unique constraint)
- [x] `DELETE /api/reviews/{id}/like` — unlike (xóa record)

### T16 — Comments (User)
- [x] Entity `Comment` (self-reference `parent_id`)
- [x] `POST /api/comments` — tạo comment
  - [x] `target_type` ∈ {REVIEW, NEWS}
  - [x] `parent_id` nullable (null = top-level, có giá trị = reply)
- [x] `DELETE /api/comments/{id}` — xóa comment của mình
- [x] `GET /api/comments?targetType=&targetId=` — list comments (có thể nested)

---

## PHASE 6 — Content (Places, Foods, News)

### T17 — Places & Foods
- [x] Entity `Place`, `Food`
- [x] Admin CRUD: `POST/PUT/DELETE /api/admin/places`
- [x] Admin CRUD: `POST/PUT/DELETE /api/admin/foods`
- [x] Public: `GET /api/places` — list (filter `is_active=true`)
- [x] Public: `GET /api/places/{slug}` — chi tiết
- [x] Public: `GET /api/foods`, `GET /api/foods/{slug}` — tương tự

### T18 — News
- [x] Entity `News`
- [x] `POST /api/admin/news` — tạo bài viết (`is_published=false` mặc định)
- [x] `PUT /api/admin/news/{id}` — cập nhật
- [x] `DELETE /api/admin/news/{id}` — xóa
- [x] `PATCH /api/admin/news/{id}/publish` — publish (`is_published=true`, set `published_at`)
- [x] `GET /api/news` — list bài đã published (Guest + User), pagination
- [x] `GET /api/news/{slug}` — chi tiết bài viết

---

## PHASE 7 — Admin Management API

### T19 — Admin: Manage Users
- [ ] `GET /api/admin/users` — list users (filter: `role`, `isActive`, search `name/email`, pagination)
- [ ] `GET /api/admin/users/{id}` — chi tiết user (kèm bookings gần đây)
- [ ] `PATCH /api/admin/users/{id}/activate` — kích hoạt tài khoản (`is_active=true`)
- [ ] `PATCH /api/admin/users/{id}/deactivate` — khóa tài khoản (`is_active=false`)

### T20 — Admin: Manage Bookings
- [ ] `GET /api/admin/bookings` — list tất cả bookings (filter: `status`, `fromDate`, `toDate`, `userId`, pagination)
- [ ] `GET /api/admin/bookings/{id}` — chi tiết booking (kèm payment info)
- [ ] `PATCH /api/admin/bookings/{id}/complete` — đánh dấu `COMPLETED`
- [ ] `PATCH /api/admin/bookings/{id}/cancel` — hủy booking bởi admin

### T21 — Admin: Manage Reviews
- [ ] `GET /api/admin/reviews` — list reviews (filter: `isApproved`, `targetType`, pagination)
- [ ] `PATCH /api/admin/reviews/{id}/approve` — duyệt review (`is_approved=true`)
- [ ] `PATCH /api/admin/reviews/{id}/reject` — từ chối / xóa review
- [ ] `DELETE /api/admin/reviews/{id}` — xóa review

### T22 — Admin: Revenue Report
- [ ] `GET /api/admin/revenue` — tổng quan
  - Params: `from`, `to` (date range)
  - Response: tổng doanh thu, số booking theo status, doanh thu theo tháng (group by)
- [ ] `GET /api/admin/revenue/tours` — top tours theo doanh thu trong kỳ

---

## PHASE 8 — Admin UI

> Chọn 1 trong 2 hướng:
> - **Option A**: Thymeleaf (server-side, trong cùng Spring Boot app)
> - **Option B**: SPA riêng (React/Vue) gọi vào Admin API

### T23 — Admin UI Setup
- [ ] Chọn approach (Thymeleaf hoặc SPA)
- [ ] Tích hợp template/layout chung: sidebar, header, breadcrumb
- [ ] Auth guard: redirect về login nếu chưa có JWT
- [ ] Trang Login admin

### T24 — Dashboard
- [ ] Thống kê nhanh: tổng user, tổng tour, booking hôm nay, doanh thu tháng này
- [ ] Chart doanh thu theo tuần/tháng

### T25 — UI: Manage Users
- [ ] Danh sách users (search, filter, pagination)
- [ ] Kích hoạt / Khóa tài khoản
- [ ] Xem chi tiết user

### T26 — UI: Manage Categories & Tours
- [ ] CRUD categories (tree view)
- [ ] CRUD tours (form tạo/sửa tour với upload ảnh)
- [ ] Quản lý schedules của từng tour
- [ ] Đổi status tour

### T27 — UI: Manage Bookings
- [ ] Danh sách bookings (filter status, date)
- [ ] Chi tiết booking kèm thông tin payment
- [ ] Cập nhật status booking

### T28 — UI: Manage Reviews
- [ ] Danh sách reviews chờ duyệt
- [ ] Approve / Reject review

### T29 — UI: Revenue Report
- [ ] Bộ lọc khoảng thời gian
- [ ] Hiển thị tổng doanh thu, bảng theo tháng
- [ ] Top tours doanh thu cao nhất

---

## PHASE 9 — Cross-cutting Concerns

### T30 — Activity Log
- [ ] Entity `Activity`
- [ ] `ActivityService.log(userId, bookingId, type, metadata)` — tái sử dụng ở T10, T11, T12
- [ ] `GET /api/users/me/activities` — user xem lịch sử hoạt động (pagination)
- [ ] `GET /api/admin/activities` — admin xem tất cả (filter `type`, `userId`)

### T31 — Validation & Error Handling
- [ ] Annotation `@Valid` trên tất cả `@RequestBody`
- [ ] Chuẩn hóa error response: `{ status, code, message, errors[] }`
- [ ] Handle: 400 validation, 401 unauthorized, 403 forbidden, 404 not found, 409 conflict, 500 server error

### T32 — Testing
- [ ] Unit test `AuthService` (register, login, refresh token)
- [ ] Unit test `BookingService` (tạo booking, validate slot, cancel)
- [ ] Unit test `PaymentService` (tạo payment, validate)
- [ ] Integration test `AuthController` (MockMvc)
- [ ] Integration test `BookingController` (MockMvc)

---

## Spring Initializr — Dependency Checklist

Khi tạo project tại [start.spring.io](https://start.spring.io), chọn:

| Dependency | Dùng cho |
|---|---|
| Spring Web | REST API |
| Spring Data JPA | ORM / Database |
| Spring Security | Auth & Authorization |
| OAuth2 Client | Facebook, Google, Twitter login |
| PostgreSQL Driver | Kết nối PostgreSQL |
| Lombok | Giảm boilerplate code |
| Validation | `@Valid`, `@NotNull`, ... |
| Flyway Migration | *(optional)* Quản lý schema version |

**Thêm thủ công vào `pom.xml` sau khi tạo project:**
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>

<!-- MapStruct (DTO mapping) -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
```

---

## Progress Tracker

| Phase | Tasks | Status |
|---|---|---|
| 1 — Foundation | T01 | ✅ |
| 2 — Auth & User | T02, T03, T04, T05 | ✅ T02 T04 T05 · ⬜ T03 |
| 3 — Category & Tour | T06, T07, T08, T09 | ✅ T06 T07 T08 T09 |
| 4 — Booking & Payment | T10, T11 | ✅ |
| 5 — Social | T12, T13, T14, T15, T16 | ✅ |
| 6 — Content | T17, T18 | ✅ |
| 7 — Admin API | T19, T20, T21, T22 | ⬜ |
| 8 — Admin UI | T23–T29 | ⬜ |
| 9 — Cross-cutting | T30, T31, T32 | ⬜ |

> ⬜ Not started · 🔄 In progress · ✅ Done
