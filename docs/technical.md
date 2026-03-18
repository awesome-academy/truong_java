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

---

## 2. Lombok

| Annotation | Sinh ra |
|---|---|
| `@Getter` | Getter cho tất cả fields |
| `@Setter` | Setter cho tất cả fields |
| `@NoArgsConstructor` | Constructor không tham số (JPA bắt buộc có) |
| `@AllArgsConstructor` | Constructor đủ tất cả tham số |
| `@Builder` | Builder pattern: `User.builder().email("...").build()` |
| `@Builder.Default` | Giữ default value khi dùng `@Builder` (VD: `role = Role.USER`) |

> Nếu không có `@Builder.Default`, `@Builder` sẽ bỏ qua giá trị khởi tạo và dùng default của Java (`null`, `false`, `0`).

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
