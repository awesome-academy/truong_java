# Booking Tours

Spring Boot · Spring Data JPA · Spring Security · JWT · OAuth2 · PostgreSQL

## Project Structure

```
src/main/java/com/sun/bookingtours/
├── config/          # Security config, Bean config, CORS,...
├── controller/      # Nhận HTTP request, trả response về client
├── service/         # Business logic (xử lý nghiệp vụ)
├── repository/      # Query database (extends JpaRepository)
├── entity/          # JPA entities — map 1-1 với DB tables
├── dto/
│   ├── request/     # Object nhận data từ client (LoginRequest, ...)
│   └── response/    # Object trả về cho client (UserResponse, ...)
├── mapper/          # Convert entity ↔ dto
├── security/        # JWT filter, UserDetails, UserPrincipal
├── exception/       # Custom exceptions, GlobalExceptionHandler
└── util/            # Helper functions dùng chung
```

## Database

PostgreSQL — schema được quản lý bởi Flyway migration.

```
src/main/resources/db/migration/
└── V1__init_schema.sql   # Tạo toàn bộ tables, indexes, triggers
```

## Setup

### Yêu cầu
- Java 21
- Docker (chạy PostgreSQL)

### Chạy PostgreSQL

```bash
docker compose up -d
```

### Chạy app

```bash
./mvnw spring-boot:run
```
