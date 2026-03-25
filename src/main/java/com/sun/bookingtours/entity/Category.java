package com.sun.bookingtours.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories")
@BatchSize(size = 20)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")     // nullable — category cha không có parent
    private Category parent;            // Self-reference: category có thể có cha cũng là category

    private String name;

    @Column(nullable = false, unique = true)
    private String slug;                // URL-friendly name, VD: "du-lich-bien"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
