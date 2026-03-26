package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.FoodRequest;
import com.sun.bookingtours.dto.response.FoodResponse;
import com.sun.bookingtours.entity.Food;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.FoodMapper;
import com.sun.bookingtours.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    public List<FoodResponse> listActive() {
        return foodRepository.findAllByIsActiveTrue().stream()
                .map(foodMapper::toResponse)
                .toList();
    }

    public FoodResponse getBySlug(String slug) {
        Food food = foodRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Food", slug));
        return foodMapper.toResponse(food);
    }

    @Transactional
    public FoodResponse create(FoodRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());

        if (foodRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        Food food = Food.builder()
                .name(request.getName())
                .slug(slug)
                .location(request.getLocation())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return foodMapper.toResponse(foodRepository.save(food));
    }

    @Transactional
    public FoodResponse update(UUID id, FoodRequest request) {
        Food food = findById(id);
        String slug = resolveSlug(request.getSlug(), request.getName());

        if (!slug.equals(food.getSlug()) && foodRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        food.setName(request.getName());
        food.setSlug(slug);
        food.setLocation(request.getLocation());
        food.setDescription(request.getDescription());
        food.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getIsActive() != null) {
            food.setActive(request.getIsActive());
        }

        return foodMapper.toResponse(food);
    }

    @Transactional
    public void delete(UUID id) {
        Food food = findById(id);
        food.setActive(false);
    }

    private Food findById(UUID id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food", id));
    }

    private String resolveSlug(String requestSlug, String name) {
        if (requestSlug != null && !requestSlug.isBlank()) {
            return requestSlug.trim().toLowerCase();
        }
        return generateSlug(name);
    }

    private String generateSlug(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d").replaceAll("Đ", "D")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
