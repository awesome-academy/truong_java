package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.PlaceRequest;
import com.sun.bookingtours.dto.response.PlaceResponse;
import com.sun.bookingtours.entity.Place;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.PlaceMapper;
import com.sun.bookingtours.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;

    public List<PlaceResponse> listActive() {
        return placeRepository.findAllByIsActiveTrue().stream()
                .map(placeMapper::toResponse)
                .toList();
    }

    public PlaceResponse getBySlug(String slug) {
        Place place = placeRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Place", slug));
        return placeMapper.toResponse(place);
    }

    @Transactional
    public PlaceResponse create(PlaceRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());

        if (placeRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        Place place = Place.builder()
                .name(request.getName())
                .slug(slug)
                .location(request.getLocation())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return placeMapper.toResponse(placeRepository.save(place));
    }

    @Transactional
    public PlaceResponse update(UUID id, PlaceRequest request) {
        Place place = findById(id);
        String slug = resolveSlug(request.getSlug(), request.getName());

        if (!slug.equals(place.getSlug()) && placeRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        place.setName(request.getName());
        place.setSlug(slug);
        place.setLocation(request.getLocation());
        place.setDescription(request.getDescription());
        place.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getIsActive() != null) {
            place.setActive(request.getIsActive());
        }

        return placeMapper.toResponse(place);
    }

    @Transactional
    public void delete(UUID id) {
        Place place = findById(id);
        place.setActive(false);
    }

    private Place findById(UUID id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Place", id));
    }

    private String resolveSlug(String requestSlug, String name) {
        if (requestSlug != null && !requestSlug.isBlank()) {
            return requestSlug.trim().toLowerCase();
        }
        return generateSlug(name);
    }

    // "Vịnh Hạ Long" → "vinh-ha-long"
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
