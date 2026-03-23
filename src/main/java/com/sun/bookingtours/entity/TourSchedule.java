package com.sun.bookingtours.entity;

import com.sun.bookingtours.entity.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tour_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;    // LocalDate cho kiểu DATE (không có giờ), LocalDateTime cho TIMESTAMP

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "total_slots", nullable = false)
    private int totalSlots;

    @Column(name = "price_override", precision = 15, scale = 2)
    private BigDecimal priceOverride;   // null = dùng base_price của tour

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.OPEN;
}
