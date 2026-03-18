package com.sun.bookingtours.entity;

import com.sun.bookingtours.entity.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;  // Polymorphic: review có thể thuộc TOUR, PLACE, hoặc FOOD

    @Column(name = "target_id", nullable = false)
    private UUID targetId;          // ID của tour/place/food tương ứng — không dùng @ManyToOne vì target có thể là nhiều loại khác nhau

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Short rating;           // SMALLINT trong DB — dùng Short thay vì int

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean isApproved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
