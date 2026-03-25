package com.sun.bookingtours.dto.response;

import com.sun.bookingtours.entity.enums.TourStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TourResponse {
    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private BigDecimal basePrice;
    private int durationDays;
    private int maxParticipants;
    private String departureLocation;
    private TourStatus status;
    private BigDecimal avgRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Chỉ trả id + name + slug của category — không cần toàn bộ tree
    private CategorySummary category;

    private List<TourImageResponse> images;

    // Tên place/food đủ dùng cho admin list/detail — không cần full object
    private List<LinkSummary> places;
    private List<LinkSummary> foods;

    // Chỉ populate ở detail endpoint — list endpoint để null (tránh query thừa)
    private List<TourScheduleResponse> schedules;

    @Data
    public static class CategorySummary {
        private UUID id;
        private String name;
        private String slug;
    }

    @Data
    public static class LinkSummary {
        private UUID id;
        private String name;
        private String slug;
    }
}
