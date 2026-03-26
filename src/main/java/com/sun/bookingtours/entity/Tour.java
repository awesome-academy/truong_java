package com.sun.bookingtours.entity;

import com.sun.bookingtours.entity.enums.TourStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)   // precision=15: tổng số chữ số, scale=2: số chữ số sau dấu phẩy
    private BigDecimal basePrice;                                                 // Dùng BigDecimal thay vì double để tránh lỗi làm tròn tiền tệ

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "departure_location")
    private String departureLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TourStatus status = TourStatus.DRAFT;

    @Column(name = "avg_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)  // mappedBy: chỉ field "tour" bên TourImage là owner của relationship
    @Builder.Default                                                                  // cascade=ALL: save/delete Tour thì tự save/delete luôn TourImage
    @BatchSize(size = 20)                                                             // orphanRemoval=true: xóa TourImage khỏi list thì tự DELETE khỏi DB
    private List<TourImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<TourSchedule> schedules = new ArrayList<>();

    @ManyToMany                                                         // Quan hệ nhiều-nhiều: 1 tour có nhiều places, 1 place thuộc nhiều tours
    @JoinTable(                                                         // @JoinTable: chỉ định bảng trung gian tour_places
        name = "tour_places",
        joinColumns = @JoinColumn(name = "tour_id"),                    // FK trỏ về bảng hiện tại (tours)
        inverseJoinColumns = @JoinColumn(name = "place_id")             // FK trỏ về bảng kia (places)
    )
    @BatchSize(size = 20)
    @Builder.Default
    private List<Place> places = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "tour_foods",
        joinColumns = @JoinColumn(name = "tour_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    @BatchSize(size = 20)
    @Builder.Default
    private List<Food> foods = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
