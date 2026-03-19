package com.sun.bookingtours.entity;

import com.sun.bookingtours.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity                    // Đánh dấu class này map với 1 table trong DB
@Table(name = "users")     // Tên table trong DB (mặc định Hibernate dùng tên class)
@Getter                    // Lombok: tự generate getter cho tất cả fields
@Setter                    // Lombok: tự generate setter
@NoArgsConstructor         // Lombok: tự generate constructor không tham số (JPA bắt buộc có)
@AllArgsConstructor        // Lombok: tự generate constructor đủ tất cả tham số
@Builder                   // Lombok: cho phép dùng builder pattern: User.builder().email("...").build()
public class User {

    @Id                                                 // Đánh dấu đây là primary key
    @GeneratedValue(strategy = GenerationType.UUID)     // Tự generate UUID khi insert
    private UUID id;

    @Column(name = "full_name", nullable = false)       // name: tên column trong DB, nullable=false: NOT NULL
    private String fullName;

    @Column(nullable = false, unique = true)            // unique=true: không được trùng email
    private String email;

    private String phone;                               // Không cần @Column nếu tên field == tên column

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)    // Lưu enum dưới dạng String ("ADMIN", "USER") thay vì số (0, 1)
    @Column(nullable = false)       // Không dùng EnumType.ORDINAL vì dễ lỗi khi thêm/xóa enum value
    @Builder.Default                // Giữ default value khi dùng builder pattern
    private Role role = Role.USER;

    @Column(name = "is_active", nullable = false)
    @Builder.Default                // Nếu không có @Builder.Default, builder sẽ set false (default của boolean)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)   // updatable=false: không cho sửa sau khi tạo
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;    // Soft delete: xóa thì set giá trị này thay vì DELETE khỏi DB

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;        // Lưu refresh token hiện tại, logout thì set null

    @PrePersist     // Hook: tự động chạy trước khi INSERT vào DB
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate      // Hook: tự động chạy trước khi UPDATE vào DB
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
