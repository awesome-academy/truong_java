# Technical Notes

---

## 1. JPA / Hibernate

### 1.1 Class-level annotations

| Annotation | Ý nghĩa |
|---|---|
| `@Entity` | Đánh dấu class map với 1 table trong DB |
| `@Table(name = "...")` | Chỉ định tên table (mặc định Hibernate dùng tên class) |

### 1.2 Field-level annotations

| Annotation | Ý nghĩa |
|---|---|
| `@Id` | Đánh dấu primary key |
| `@GeneratedValue(strategy = GenerationType.UUID)` | Tự generate UUID khi insert |
| `@Column(name, nullable, unique, updatable, columnDefinition, precision, scale)` | Tùy chỉnh column trong DB |
| `@Enumerated(EnumType.STRING)` | Lưu enum dạng String ("ADMIN") thay vì số (0) — tránh lỗi khi thêm/xóa enum value |
| `@JdbcTypeCode(SqlTypes.JSON)` | Map field với kiểu JSONB của PostgreSQL |

> **`updatable = false`** — dùng cho `created_at`: không cho Hibernate UPDATE column này sau khi INSERT.

> **`columnDefinition = "TEXT"`** — dùng kiểu TEXT của PostgreSQL thay vì VARCHAR (không giới hạn độ dài).

> **`precision = 15, scale = 2`** — dùng cho `NUMERIC(15,2)`: 15 tổng chữ số, 2 chữ số sau dấu phẩy.

### 1.3 Lifecycle hooks

| Annotation | Khi nào chạy |
|---|---|
| `@PrePersist` | Trước khi INSERT — dùng để set `created_at`, `updated_at` |
| `@PreUpdate` | Trước khi UPDATE — dùng để set `updated_at` |

### 1.4 Quan hệ giữa các entity

| Annotation | Quan hệ | Ví dụ |
|---|---|---|
| `@ManyToOne` | Nhiều → 1 | Nhiều `OAuthAccount` thuộc 1 `User` |
| `@OneToMany` | 1 → Nhiều | 1 `Tour` có nhiều `TourImage` |
| `@OneToOne` | 1 ↔ 1 | 1 `Booking` có đúng 1 `Payment` |
| `@ManyToMany` | Nhiều ↔ Nhiều | `Tour` ↔ `Place` qua bảng `tour_places` |

**`fetch`:**
- `FetchType.LAZY` *(khuyên dùng)* — chỉ load quan hệ khi gọi getter, tránh query thừa
- `FetchType.EAGER` — load luôn khi query entity, dễ gây N+1 query problem

**`@OneToMany` options:**
```java
@OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
```
- `mappedBy = "tour"` — chỉ field `tour` bên `TourImage` là owner của relationship (giữ FK)
- `cascade = ALL` — save/delete Tour thì tự động save/delete TourImage theo
- `orphanRemoval = true` — xóa TourImage khỏi list `images` thì tự DELETE khỏi DB

**`@ManyToMany` + `@JoinTable`:**
```java
@ManyToMany
@JoinTable(
    name = "tour_places",
    joinColumns = @JoinColumn(name = "tour_id"),         // FK trỏ về bảng hiện tại
    inverseJoinColumns = @JoinColumn(name = "place_id")  // FK trỏ về bảng kia
)
```

### 1.5 Patterns đặc biệt

**Self-reference** — `Category` có `parent_id` trỏ về chính `Category`:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private Category parent;
```

**Polymorphic** — `Review`/`Comment` có thể thuộc nhiều loại entity khác nhau (TOUR, PLACE, FOOD...):
```java
@Enumerated(EnumType.STRING)
private TargetType targetType;  // TOUR | PLACE | FOOD

private UUID targetId;          // ID của target tương ứng
```
Đánh đổi: không có FK constraint ở DB, phải validate ở tầng service.

### 1.6 Kiểu dữ liệu

| Java | PostgreSQL | Dùng khi |
|---|---|---|
| `UUID` | `UUID` | Primary key |
| `String` | `VARCHAR` / `TEXT` | Text |
| `BigDecimal` | `NUMERIC(15,2)` | Tiền tệ — tránh lỗi làm tròn của `double`/`float` |
| `LocalDate` | `DATE` | Ngày (không có giờ) |
| `LocalDateTime` | `TIMESTAMP` | Ngày + giờ |
| `Short` | `SMALLINT` | Số nhỏ, VD: rating (1–5) |
| `boolean` | `BOOLEAN` | True/false |
| `Map<String, Object>` | `JSONB` | Dữ liệu động, không cố định cấu trúc |

### 1.7 N+1 Problem & MultipleBagFetchException

#### N+1 Problem

Khi query N entities rồi lazy load collection của từng cái trong vòng lặp → tổng 1 + N queries:

```java
List<Tour> tours = tourRepository.findAll();  // 1 query

for (Tour t : tours) {
    t.getImages();  // N queries — 1 query per tour
}
// 10 tours → 11 queries. 100 tours → 101 queries.
```

**Giải pháp: JOIN FETCH** — load collection ngay trong query chính, không lazy load:

```java
@Query("SELECT t FROM Tour t LEFT JOIN FETCH t.images WHERE t.slug = :slug")
Optional<Tour> findBySlugWithImages(@Param("slug") String slug);
// → 1 query duy nhất, images được load sẵn
```

Hibernate dịch JOIN FETCH thành SQL JOIN thông thường. DB trả về các rows đã gộp, Hibernate tự de-dup lại thành object đúng nhờ `@Id`.

**Ví dụ — tour có 3 images, 4 places:**

```
-- DB trả về: 3 × 4 = 12 rows (cartesian product)
row | image_url | place_name
----|-----------|------------
1   | anh1.jpg  | Hạ Long Bay
2   | anh1.jpg  | Sapa         ← anh1.jpg lặp
3   | anh1.jpg  | Hội An       ← anh1.jpg lặp
4   | anh1.jpg  | Đà Nẵng      ← anh1.jpg lặp
5   | anh2.jpg  | Hạ Long Bay  ← Hạ Long Bay lặp
...
12  | anh3.jpg  | Đà Nẵng

-- Hibernate de-dup → Java object đúng:
Tour { images: [anh1, anh2, anh3], places: [Hạ Long, Sapa, Hội An, Đà Nẵng] }
```

De-dup tự động nhờ **1st-level cache (persistence context)** — Hibernate track object theo `@Id`, thấy cùng ID thì dùng lại object cũ thay vì tạo mới.

#### MultipleBagFetchException

`Bag` = thuật ngữ Hibernate cho `java.util.List` không có order index — cho phép trùng lặp, không có thứ tự cố định.

**Vấn đề:** JOIN FETCH 2 `List` (Bag) cùng lúc sinh cartesian product cực lớn:
- 100 images × 50 places = **5,000 rows** chỉ để lấy 150 items thực sự
- Hibernate ném exception **trước khi chạy** để bảo vệ khỏi query tốn tài nguyên

```java
// ❌ Ném MultipleBagFetchException — mọi phiên bản Hibernate (kể cả 6.x, 7.x)
SELECT t FROM Tour t
LEFT JOIN FETCH t.images   -- List 1
LEFT JOIN FETCH t.places   -- List 2
LEFT JOIN FETCH t.foods    -- List 3
```

> **Lưu ý:** `MultipleBagFetchException` **vẫn còn** trong Hibernate 6.x / 7.x (Spring Boot 3.x / 4.x). Hibernate chưa bao giờ tự xử lý trường hợp này — exception được ném ngay lúc build query để bảo vệ khỏi cartesian product.

#### Các cách xử lý

| Cách | Khi nào dùng | Đánh đổi |
|---|---|---|
| **Tách query riêng** | Collection lớn, cần kiểm soát chặt | Nhiều round-trip DB hơn |
| **`Set` thay `List`** | Collection nhỏ, không cần thứ tự | Mất thứ tự chèn, cần `equals/hashCode` đúng |
| **`@OrderColumn`** | Cần giữ `List` nhưng tránh bag | Thêm cột index trong DB |
| **`@BatchSize`** | List nhiều records (giống `WHERE IN` của Laravel) | Cần config thêm |

**Rule of thumb:**
```
1 record  + collection nhỏ → JOIN FETCH ổn
N records + collection     → tách query riêng hoặc @BatchSize
```

**`@BatchSize` — cách hoạt động:**

Thay vì lazy-load từng collection một, Hibernate gom tất cả ID chưa load trong session lại và fire 1 query `WHERE id = any (?)`:

```
-- Không có @BatchSize: N+1
SELECT * FROM tour_places WHERE tour_id = 'aaa'   -- tour 1
SELECT * FROM tour_places WHERE tour_id = 'bbb'   -- tour 2
SELECT * FROM tour_places WHERE tour_id = 'ccc'   -- tour 3

-- Có @BatchSize(size = 20): 1 batch query
SELECT * FROM tour_places WHERE tour_id = any ('{aaa, bbb, ccc}')
```

Logic Hibernate:
```
mapper access tour1.getPlaces()
  → Hibernate thấy tour2.places, tour3.places cũng chưa load
  → gom tối đa size=20 ID → fire 1 query
  → set kết quả cho cả 3 tour cùng lúc

mapper access tour2.getPlaces() → đã có trong cache → không query thêm
mapper access tour3.getPlaces() → đã có trong cache → không query thêm
```

**`size` nên đặt bao nhiêu:**
- Nếu page size = 10 → `@BatchSize(size = 20)` đảm bảo luôn 1 query per collection
- Nếu page có 100 tour và size=20 → `ceil(100/20) = 5` queries per collection (vẫn tốt hơn 100)
- **Rule:** `size >= page size tối đa của app`

**`@BatchSize` đặt ở đâu:**
```java
// Collection (OneToMany / ManyToMany) → đặt trên field
@OneToMany(mappedBy = "tour")
@BatchSize(size = 20)
private List<TourImage> images;

// ManyToOne lazy → đặt trên CLASS của entity target (không phải field)
@Entity
@BatchSize(size = 20)   // ← đặt ở Category, không phải ở Tour
public class Category { ... }
```

**So sánh với Laravel Eloquent `with()`:**
Laravel không dùng JOIN — chạy query riêng `WHERE IN` cho từng collection, không có cartesian product, không bao giờ gặp vấn đề này. `@BatchSize` về bản chất giống cách Laravel xử lý.

---

## 2. Java `record` vs `class`

`record` là tính năng từ Java 16, sinh ra để thay thế class chỉ dùng để chứa data.

### 2.1 So sánh

```java
// Class thông thường — phải tự viết (hoặc dùng Lombok)
public class TourScheduleRequest {
    private LocalDate departureDate;
    private int totalSlots;

    public TourScheduleRequest(LocalDate departureDate, int totalSlots) { ... }
    public LocalDate getDepartureDate() { ... }
    public int getTotalSlots() { ... }
    public boolean equals(Object o) { ... }
    public int hashCode() { ... }
}

// Record — Java tự sinh tất cả ở trên
public record TourScheduleRequest(LocalDate departureDate, int totalSlots) {}
```

### 2.2 Đặc điểm của `record`

| Đặc điểm | Chi tiết |
|---|---|
| **Immutable** | Tất cả fields là `final` — không có setter, không thể thay đổi sau khi tạo |
| **Getter khác tên** | `departureDate()` thay vì `getDepartureDate()` — không có prefix `get` |
| **Không kế thừa được** | `record` không thể `extends` class khác (nhưng vẫn `implements` interface được) |
| **Compact constructor** | Có thể validate trong constructor mà không cần khai báo lại params |

### 2.3 Khi nào dùng gì?

| | `record` | `class` |
|---|---|---|
| **DTO** (Request/Response) | ✅ Chuẩn — chỉ truyền data, không cần thay đổi | ✅ Dùng được nhưng thừa code |
| **JPA Entity** | ❌ Không dùng — JPA cần no-arg constructor + setter để hydrate từ DB | ✅ Bắt buộc |
| **Service, Repository** | ❌ | ✅ Bắt buộc |
| **Value Object** (Money, Coordinate...) | ✅ Immutable là đúng bản chất | ✅ Dùng được |

**Quy tắc nhớ nhanh:**
```
Chỉ chứa data, không có logic, không thay đổi sau khi tạo → dùng record
Còn lại → dùng class
```

### 2.4 Compact constructor (validate đầu vào)

```java
public record TourScheduleRequest(LocalDate departureDate, LocalDate returnDate, int totalSlots) {
    // Compact constructor — không cần khai báo lại params, tự nhận từ record header
    public TourScheduleRequest {
        if (returnDate.isBefore(departureDate)) {
            throw new IllegalArgumentException("returnDate phải sau departureDate");
        }
        if (totalSlots <= 0) {
            throw new IllegalArgumentException("totalSlots phải > 0");
        }
    }
}
```

---

## 3. Lombok

| Annotation | Sinh ra |
|---|---|
| `@Getter` | Getter cho tất cả fields |
| `@Setter` | Setter cho tất cả fields |
| `@NoArgsConstructor` | Constructor không tham số (JPA bắt buộc có) |
| `@AllArgsConstructor` | Constructor đủ tất cả tham số |
| `@Builder` | Builder pattern: `User.builder().email("...").build()` |
| `@Builder.Default` | Giữ default value khi dùng `@Builder` (VD: `role = Role.USER`) |

> Nếu không có `@Builder.Default`, `@Builder` sẽ bỏ qua giá trị khởi tạo và dùng default của Java (`null`, `false`, `0`).

### 2.1 Tại sao JPA Entity không dùng `@Data`?

`@Data` = `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor`

Nghe tiện, nhưng 2 cái cuối gây vấn đề với JPA:

**Vấn đề 1: `@EqualsAndHashCode`**
Generate `equals()` và `hashCode()` dựa trên **tất cả fields**, kể cả các collection (`@OneToMany`, `@ManyToMany`).
Khi JPA chưa fetch lazy collection mà gọi `equals()` → trigger lazy load ngoài transaction → crash `LazyInitializationException`.

**Vấn đề 2: `@ToString`**
Generate `toString()` bao gồm tất cả fields. Nếu có quan hệ `@OneToMany` → có thể trigger fetch toàn bộ danh sách con → **N+1 query** hoặc **StackOverflowError** nếu 2 entity reference lẫn nhau.

**Kết luận:**

| Annotation | JPA Entity | DTO |
|---|---|---|
| `@Getter` + `@Setter` | ✅ An toàn | ✅ OK |
| `@Data` | ❌ Tránh dùng | ✅ Dùng được |

DTO không có quan hệ JPA, không có lazy load → `@Data` dùng thoải mái.

---

## 3. Flyway

Tool quản lý version cho DB schema — thay vì chạy SQL tay mỗi lần setup môi trường mới.

**Naming convention bắt buộc:**
```
V1__ten_file.sql    ← chạy đầu tiên
V2__them_bang.sql   ← chạy sau
```

**Flow khi app start:**
```
App start → Flyway kiểm tra bảng flyway_schema_history
          → Chưa chạy file nào? → Chạy V1, V2, ... theo thứ tự
          → Đã chạy rồi? → Bỏ qua
```

**Khi cần thay đổi schema:** thêm file `V2__...sql` mới, KHÔNG sửa file cũ.

---

## 4. `ddl-auto` modes

| Giá trị | Hành vi | Dùng khi |
|---|---|---|
| `create` | Xóa và tạo lại toàn bộ schema | Dev nhanh, không quan tâm data |
| `update` | Tự update schema theo entity | Prototype — nguy hiểm ở production |
| `validate` | Kiểm tra entity có khớp DB không, báo lỗi nếu sai | Production (dùng với Flyway) |
| `none` | Không làm gì | Khi tự quản lý schema hoàn toàn |

> Project này dùng `validate` + Flyway: Flyway tạo tables, Hibernate chỉ kiểm tra.

---

## 5. Exception Handling

### 5.1 Checked vs Unchecked Exception

| | Checked (`extends Exception`) | Unchecked (`extends RuntimeException`) |
|---|---|---|
| Compiler kiểm tra | Có — bắt buộc try/catch hoặc khai báo `throws` | Không |
| Dùng khi | Lỗi từ bên ngoài (file, network, DB driver) | Lỗi logic nghiệp vụ trong code |
| Spring REST API | Rất ít dùng | Chuẩn |

**Dễ nhớ:**
```
Checked   = lỗi từ thế giới bên ngoài → xử lý ngay tại chỗ
Unchecked = lỗi logic trong code của mày → vứt lên, GlobalExceptionHandler nhặt
```

**Checked — phải xử lý ngay tại chỗ:**
```java
// Compiler bắt buộc khai báo throws
public String readFile(String path) throws IOException {
    return Files.readString(Path.of(path));
}

// Nơi gọi bắt buộc try/catch
try {
    readFile("/data/config.txt");
} catch (IOException e) {
    // xử lý ngay ở đây
}
```

**Unchecked — vứt tự do, GlobalExceptionHandler nhặt:**
```java
// Không cần khai báo throws
public Tour findById(UUID id) {
    return tourRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Tour", id));
}

// Nơi gọi không cần try/catch
tourService.findById(id);  // nếu throw → GlobalExceptionHandler tự bắt → trả 404
```

### 5.2 Custom Exceptions trong project

| Class | Dùng khi | HTTP status |
|---|---|---|
| `ResourceNotFoundException` | Không tìm thấy resource (tour, user, booking...) | 404 |
| `BusinessException` | Vi phạm nghiệp vụ (không đủ slot, booking đã hủy...) | 400 |
| `UnauthorizedException` | Không có quyền truy cập | 401 / 403 |

```java
// Ví dụ dùng trong service
throw new ResourceNotFoundException("Tour", id);
// → message: "Tour not found with id: abc-123"

throw new BusinessException("Không đủ slot cho schedule này");

throw new UnauthorizedException("Bạn không có quyền hủy booking này");
```

### 5.3 GlobalExceptionHandler

```java
@RestControllerAdvice   // Đăng ký class này là "trạm bắt exception" toàn app
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)  // Chỉ định method này xử lý exception nào
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }
}
```

**`@RestControllerAdvice`** — Spring tự động đăng ký, mọi exception throw từ bất kỳ controller nào đều chạy qua đây.

**`@ExceptionHandler(X.class)`** — mỗi method bắt 1 loại exception.

**`<?>`** — wildcard generic, nghĩa là "T là gì cũng được". Dùng cho response lỗi vì không có data, T không quan trọng.

**Flow:**
```
Service: throw new BusinessException("Không đủ slot")
        ↓
Spring tìm @ExceptionHandler phù hợp
        ↓
handler chạy → trả ResponseEntity với HTTP status tương ứng
        ↓
Client nhận: { "success": false, "message": "Không đủ slot" }
```

**HTTP status theo loại exception:**

| Exception | HTTP Status |
|---|---|
| `ResourceNotFoundException` | 404 Not Found |
| `BusinessException` | 400 Bad Request |
| `UnauthorizedException` | 401 Unauthorized |

---

## 6. Spring Security & JWT

### 6.1 Các file liên quan

| File | Làm gì |
|---|---|
| [SecurityConfig.java](../src/main/java/com/sun/bookingtours/config/SecurityConfig.java) | Cấu hình filter chain, phân quyền route, khai báo beans |
| [JwtTokenProvider.java](../src/main/java/com/sun/bookingtours/security/JwtTokenProvider.java) | Generate / validate / parse JWT token |
| [JwtAuthenticationFilter.java](../src/main/java/com/sun/bookingtours/security/JwtAuthenticationFilter.java) | Chạy trước mỗi request, đọc token từ header và set user vào SecurityContext |
| [UserPrincipal.java](../src/main/java/com/sun/bookingtours/security/UserPrincipal.java) | Wrapper của `User` entity, implements `UserDetails` để Spring Security hiểu |
| [UserDetailsServiceImpl.java](../src/main/java/com/sun/bookingtours/security/UserDetailsServiceImpl.java) | Load user từ DB theo email khi Spring Security cần xác thực |
| [AuthService.java](../src/main/java/com/sun/bookingtours/service/AuthService.java) | Business logic: register, login, refresh, logout |
| [AuthController.java](../src/main/java/com/sun/bookingtours/controller/AuthController.java) | Expose 4 endpoint: `POST /api/auth/*` |

---

### 6.2 Tổng quan flow

```
Request → [JwtAuthenticationFilter] → [AuthorizationFilter] → Controller
```

- **Public route** (`/api/auth/**`, `GET /api/tours/**`...): filter bỏ qua, không cần token
- **Protected route**: filter đọc token → validate → load user → set vào `SecurityContextHolder`
- **Admin route** (`/api/admin/**`): sau filter còn check thêm `ROLE_ADMIN`

---

### 6.3 JWT — Cấu trúc token

```
xxxxx.yyyyy.zzzzz
  |      |      |
Header  Payload  Signature
```

- **Payload** chứa: `email`, `userId`, `role`, `exp` (thời gian hết hạn)
- **Signature** = HMAC-SHA256(header + payload, secretKey) — chỉ server biết key → không thể giả mạo
- Token là **public** (ai cũng decode được) → không được nhét password hay data nhạy cảm vào

**Access token** — sống 15 phút, gửi kèm mỗi request
**Refresh token** — sống 7 ngày, chỉ dùng để lấy access token mới. Lưu vào DB để có thể invalidate khi logout.

---

### 6.4 Data flow: Register

```
Client                          AuthController              AuthService                  DB
  |                                   |                          |                        |
  |-- POST /api/auth/register ------> |                          |                        |
  |   { fullName, email, password }   |                          |                        |
  |                                   |-- authService.register() |                        |
  |                                   |                          |-- existsByEmail() ----> |
  |                                   |                          | <-- false ------------- |
  |                                   |                          |                        |
  |                                   |                          | BCrypt.encode(password) |
  |                                   |                          |                        |
  |                                   |                          |-- userRepo.save() ----> |
  |                                   |                          | <-- User (có id) ------- |
  |                                   |                          |                        |
  |                                   |                          | generateAccessToken()   |
  |                                   |                          | generateRefreshToken()  |
  |                                   |                          |-- save refreshToken --> |
  |                                   |                          |                        |
  | <-- 200 { accessToken,            | <-- AuthResponse ------- |                        |
  |           refreshToken }          |                          |                        |
```

**Các bước:**
1. `@Valid` kiểm tra input: email đúng format, password ≥ 6 ký tự → sai: trả 400 ngay
2. `existsByEmail()` → email tồn tại rồi: throw `BusinessException` → 400
3. `BCrypt.encode()` → hash password trước khi lưu, không bao giờ lưu plain-text
4. `userRepo.save()` → INSERT, `@PrePersist` tự set `createdAt`, `updatedAt`
5. `generateTokens()` → tạo cặp token, lưu refresh token vào DB

---

### 6.5 Data flow: Login

```
Client                          AuthController              AuthService                  DB
  |                                   |                          |                        |
  |-- POST /api/auth/login ---------->|                          |                        |
  |   { email, password }             |                          |                        |
  |                                   |-- authService.login()    |                        |
  |                                   |              AuthenticationManager.authenticate() |
  |                                   |                          | loadUserByUsername()    |
  |                                   |                          |-- findByEmail() ------> |
  |                                   |                          | <-- User -------------- |
  |                                   |                          |                        |
  |                                   |                          | BCrypt.matches()        |
  |                                   |                          | → sai: 401              |
  |                                   |                          | → đúng: Authentication  |
  |                                   |                          |                        |
  |                                   |                          | generateAccessToken()   |
  |                                   |                          | generateRefreshToken()  |
  |                                   |                          |-- save refreshToken --> |
  |                                   |                          |                        |
  | <-- 200 { accessToken,            | <-- AuthResponse ------- |                        |
  |           refreshToken }          |                          |                        |
```

**Các bước:**
1. `AuthenticationManager.authenticate()` — Spring Security tự gọi `loadUserByUsername(email)` ngầm
2. `BCrypt.matches(rawPassword, hash)` — sai: throw `BadCredentialsException` → 401
3. Đúng → lấy `UserPrincipal` từ `Authentication` → generate token
4. Lưu refresh token mới vào DB (ghi đè nếu login lại)

---

### 6.6 Data flow: Mọi request sau khi đã login

```
Client                    JwtAuthenticationFilter       UserDetailsService        Controller
  |                               |                            |                      |
  |-- GET /api/bookings --------> |                            |                      |
  |   Authorization: Bearer xxx   |                            |                      |
  |                               | extractToken() → "xxx"     |                      |
  |                               | validateToken() → true     |                      |
  |                               | getEmailFromToken()        |                      |
  |                               | → "test@gmail.com"         |                      |
  |                               |                            |                      |
  |                               |-- loadUserByUsername() --> |                      |
  |                               | <-- UserPrincipal -------- |                      |
  |                               |                            |                      |
  |                               | SecurityContextHolder      |                      |
  |                               | .setAuthentication()       |                      |
  |                               |                            |                      |
  |                               |-------- filterChain.doFilter() ----------------> |
  |                               |                            |        xử lý request |
  | <---------------------------------------- 200 response ----------------------- < |
```

`SecurityContextHolder` lưu theo **thread-local** — chỉ sống trong request hiện tại, không share giữa các request. Đây là lý do mỗi request đều phải gửi kèm token.

---

### 6.7 BCrypt

- `encode("rawPassword")` → hash khác nhau mỗi lần (do salt ngẫu nhiên)
- `matches("rawPassword", hash)` → `true/false`
- **Không bao giờ lưu plain-text password vào DB**

---

## 7. Transaction & Propagation

### 7.1 `@Transactional` cơ bản

| Attribute | Mặc định | Dùng khi |
|---|---|---|
| `propagation` | `REQUIRED` | — |
| `readOnly` | `false` | Set `true` cho query-only → Hibernate bỏ dirty checking, tiết kiệm memory |

### 7.2 Transaction Propagation

Xác định **hành vi của transaction** khi một `@Transactional` method gọi một `@Transactional` method khác.

| Giá trị | Hành vi |
|---|---|
| `REQUIRED` *(mặc định)* | Dùng chung transaction của caller nếu có, tự tạo nếu chưa có |
| `REQUIRES_NEW` | Luôn tạo transaction mới, **tạm dừng** transaction của caller |

### 7.3 Khi nào dùng `REQUIRES_NEW`?

Khi muốn **tách biệt lifecycle** của 2 operation — cái này commit/rollback không ảnh hưởng cái kia.

### 7.4 Activity log — `@TransactionalEventListener` + `REQUIRES_NEW`

Activity log dùng **event-driven** thay vì gọi trực tiếp, kết hợp 2 annotation:

```
BookingService (@Transactional)
│
│  bookingRepository.save(booking)       ← transaction A
│
│  eventPublisher.publishEvent(...)      ← chỉ publish event, chưa chạy gì
│
│  [transaction A COMMIT thành công]
│       │
│       └─► ActivityService.log()        ← AFTER_COMMIT → chỉ chạy khi A commit xong
│               └─ REQUIRES_NEW          → mở transaction B mới, độc lập
│               └─ activityRepository.save()
│
│  [nếu BookingService rollback]         ← event không bắn → activity KHÔNG được lưu
```

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void log(ActivityLogEvent event) {
    activityRepository.save(Activity.builder()
            .user(event.user()).booking(event.booking()).type(event.type()).build());
}
```

**Tại sao dùng `AFTER_COMMIT` thay vì gọi trực tiếp với `REQUIRES_NEW`?**

Nếu gọi trực tiếp với `REQUIRES_NEW`, activity được lưu **trước khi** transaction cha commit — lúc đó booking chưa tồn tại trên DB → vi phạm FK constraint.

`AFTER_COMMIT` đảm bảo booking đã commit xong trước khi activity được ghi → FK luôn thoả.

**Trade-off:** Nếu BookingService rollback, activity sẽ **không** được lưu (khác với `REQUIRES_NEW` gọi trực tiếp). Đây là hành vi mong muốn — không nên log activity cho một booking không tồn tại.
