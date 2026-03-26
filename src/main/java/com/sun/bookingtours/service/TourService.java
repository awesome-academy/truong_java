package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.TourImageRequest;
import com.sun.bookingtours.dto.request.TourLinksRequest;
import com.sun.bookingtours.dto.request.TourRequest;
import com.sun.bookingtours.dto.request.TourStatusRequest;
import com.sun.bookingtours.dto.response.TourResponse;
import com.sun.bookingtours.dto.response.TourScheduleResponse;
import com.sun.bookingtours.entity.Category;
import com.sun.bookingtours.entity.Tour;
import com.sun.bookingtours.entity.TourImage;
import com.sun.bookingtours.entity.enums.ScheduleStatus;
import com.sun.bookingtours.entity.enums.TourStatus;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.TourMapper;
import com.sun.bookingtours.mapper.TourScheduleMapper;
import com.sun.bookingtours.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final TourImageRepository tourImageRepository;
    private final CategoryRepository categoryRepository;
    private final PlaceRepository placeRepository;
    private final FoodRepository foodRepository;
    private final TourScheduleRepository scheduleRepository;
    private final TourMapper tourMapper;
    private final TourScheduleMapper scheduleMapper;

    @Transactional
    public TourResponse create(TourRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getTitle());

        if (tourRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        Tour tour = Tour.builder()
                .title(request.getTitle())
                .slug(slug)
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .basePrice(request.getBasePrice())
                .durationDays(request.getDurationDays())
                .maxParticipants(request.getMaxParticipants())
                .departureLocation(request.getDepartureLocation())
                .category(resolveCategory(request.getCategoryId()))
                // status mặc định DRAFT — tour mới chưa public, admin phải chủ động ACTIVE
                .status(TourStatus.DRAFT)
                .build();

        return tourMapper.toResponse(tourRepository.save(tour));
    }

    @Transactional
    public TourResponse update(UUID id, TourRequest request) {
        Tour tour = findById(id);

        String slug = resolveSlug(request.getSlug(), request.getTitle());

        if (!slug.equals(tour.getSlug()) && tourRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        tour.setTitle(request.getTitle());
        tour.setSlug(slug);
        tour.setDescription(request.getDescription());
        tour.setThumbnailUrl(request.getThumbnailUrl());
        tour.setBasePrice(request.getBasePrice());
        tour.setDurationDays(request.getDurationDays());
        tour.setMaxParticipants(request.getMaxParticipants());
        tour.setDepartureLocation(request.getDepartureLocation());
        tour.setCategory(resolveCategory(request.getCategoryId()));

        // Không cần save() — dirty checking của JPA tự UPDATE khi transaction commit
        return tourMapper.toResponse(tour);
    }

    @Transactional
    public void delete(UUID id) {
        Tour tour = findById(id);

        // Soft delete: ghi thời điểm xóa, không DELETE thật khỏi DB
        // Lý do: booking/payment đang tham chiếu tour — xóa thật sẽ phá data lịch sử
        tour.setDeletedAt(LocalDateTime.now());
    }

    @Transactional
    public TourResponse updateStatus(UUID id, TourStatusRequest request) {
        Tour tour = findById(id);
        tour.setStatus(request.getStatus());
        return tourMapper.toResponse(tour);
    }

    @Transactional
    public TourResponse addImages(UUID id, TourImageRequest request) {
        Tour tour = findById(id);

        // sortOrder tính từ max hiện có + 1, không dùng size()
        // vì nếu đã xóa ảnh giữa chừng, size() < max(sortOrder) → trùng index
        int nextOrder = tour.getImages().stream()
                .mapToInt(TourImage::getSortOrder)
                .max()
                .orElse(-1) + 1;

        for (String url : request.getImageUrls()) {
            TourImage image = TourImage.builder()
                    .tour(tour)
                    .imageUrl(url)
                    .sortOrder(nextOrder++)
                    .build();
            tour.getImages().add(image);
        }

        // cascade=ALL trên Tour.images → save tour tự save luôn TourImage mới
        return tourMapper.toResponse(tourRepository.save(tour));
    }

    @Transactional
    public void deleteImage(UUID tourId, UUID imageId) {
        // Validate ảnh thuộc đúng tour — tránh admin xóa nhầm ảnh tour khác
        TourImage image = tourImageRepository.findByIdAndTourId(imageId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("TourImage", imageId));

        // orphanRemoval=true trên Tour.images → xóa khỏi list thì tự DELETE DB
        image.getTour().getImages().remove(image);
    }

    @Transactional
    public TourResponse updatePlaces(UUID id, TourLinksRequest request) {
        Tour tour = findById(id);

        // Thay toàn bộ danh sách places — không merge từng cái
        // Lý do: đơn giản hơn, UI gửi lên list đầy đủ sau khi chỉnh sửa
        List<UUID> placeIds = request.getIds();
        var places = placeRepository.findAllByIdIn(placeIds);
        if (places.size() != placeIds.size()) {
            throw new BusinessException("Một hoặc nhiều place ID không tồn tại");
        }
        tour.setPlaces(places);
        return tourMapper.toResponse(tour);
    }

    @Transactional
    public TourResponse updateFoods(UUID id, TourLinksRequest request) {
        Tour tour = findById(id);
        List<UUID> foodIds = request.getIds();
        var foods = foodRepository.findAllByIdIn(foodIds);
        if (foods.size() != foodIds.size()) {
            throw new BusinessException("Một hoặc nhiều food ID không tồn tại");
        }
        tour.setFoods(foods);
        return tourMapper.toResponse(tour);
    }

    // ---- Public API (Guest + User) ----

    @Transactional(readOnly = true)
    public Page<TourResponse> listPublic(UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                         Integer durationDays, String departureLocation, Pageable pageable) {
        // Ghép các Specification lại — null spec tự động bị bỏ qua
        Specification<Tour> spec = Specification
                .where(TourSpecification.isPublic())
                .and(TourSpecification.hasCategory(categoryId))
                .and(TourSpecification.minPrice(minPrice))
                .and(TourSpecification.maxPrice(maxPrice))
                .and(TourSpecification.hasDuration(durationDays))
                .and(TourSpecification.hasDeparture(departureLocation));

        // Page<Tour> → Page<TourResponse>: map() giữ nguyên pagination metadata
        return tourRepository.findAll(spec, pageable).map(tourMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TourResponse getPublicDetail(String slug) {
        Tour tour = tourRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", slug));

        TourResponse response = tourMapper.toResponse(tour);

        // Load schedules riêng — schedules không được JOIN FETCH trong query chính
        // vì chỉ cần trả OPEN schedules, không phải tất cả
        List<TourScheduleResponse> schedules = scheduleRepository
                .findByTourIdAndStatus(tour.getId(), ScheduleStatus.OPEN)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();

        response.setSchedules(schedules);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<TourResponse> search(String q, Pageable pageable) {
        return tourRepository.search(q, pageable).map(tourMapper::toResponse);
    }

    // ---- private helpers ----

    private Tour findById(UUID id) {
        // Dùng findByIdWithDetails để JOIN FETCH images sẵn — places/foods lazy-load trong transaction
        return tourRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", id));
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }

    private String resolveSlug(String slug, String title) {
        if (slug != null && !slug.isBlank()) {
            return slug.trim().toLowerCase();
        }
        return generateSlug(title);
    }

    private String generateSlug(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d").replaceAll("Đ", "D")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
