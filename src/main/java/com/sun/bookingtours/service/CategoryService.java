package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.CategoryRequest;
import com.sun.bookingtours.dto.response.CategoryResponse;
import com.sun.bookingtours.entity.Category;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.CategoryMapper;
import com.sun.bookingtours.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // Trả về toàn bộ category dạng tree (chỉ active)
    public List<CategoryResponse> getTree() {
        // Lấy flat list 1 query (JOIN FETCH tránh N+1)
        List<Category> all = categoryRepository.findAllActiveWithParent();

        // Group theo parentId để tra cứu nhanh children của từng node
        // key = parentId (UUID), value = list category con
        Map<UUID, List<Category>> byParent = all.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        // Chỉ lấy root (không có cha), rồi đệ quy gắn children
        return all.stream()
                .filter(c -> c.getParent() == null)
                .map(root -> buildNode(root, byParent))
                .toList();
    }

    // Đệ quy: map entity → DTO, sau đó gắn children vào
    private CategoryResponse buildNode(Category category, Map<UUID, List<Category>> byParent) {
        CategoryResponse response = categoryMapper.toResponse(category);

        List<Category> children = byParent.getOrDefault(category.getId(), List.of());
        response.setChildren(
                children.stream()
                        .map(child -> buildNode(child, byParent))
                        .toList()
        );
        return response;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = resolveSlug(request);

        // Slug phải unique — không cho tạo nếu trùng
        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .parent(resolveParent(request.getParentId()))
                .build();

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = findById(id);

        String slug = resolveSlug(request);

        // Chỉ check trùng slug nếu slug mới khác slug hiện tại
        if (!slug.equals(category.getSlug()) && categoryRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setParent(resolveParent(request.getParentId()));

        // Không cần save() — JPA dirty checking tự UPDATE khi @Transactional kết thúc
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = findById(id);

        // Chặn xóa nếu còn category con active
        // Lý do: xóa cha mà con vẫn tồn tại → con bị treo (parent trỏ vào bản ghi đã inactive)
        List<Category> activeChildren = categoryRepository.findByParentIdAndIsActiveTrue(id);
        if (!activeChildren.isEmpty()) {
            throw new BusinessException("Không thể xóa category vì còn " + activeChildren.size() + " category con");
        }

        // Soft delete: chỉ đánh dấu inactive, không xóa thật
        // Lý do: tour đang dùng category này vẫn còn data, xóa thật sẽ mất liên kết
        category.setActive(false);
    }

    // Trả Map thay vì DTO — tránh SpEL restriction của Spring Framework 7 trong Thymeleaf
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getCategoryForEdit(UUID id) {
        Category cat = findById(id);
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", cat.getId().toString());
        m.put("name", cat.getName() != null ? cat.getName() : "");
        m.put("slug", cat.getSlug() != null ? cat.getSlug() : "");
        m.put("description", cat.getDescription() != null ? cat.getDescription() : "");
        m.put("imageUrl", cat.getImageUrl() != null ? cat.getImageUrl() : "");
        m.put("active", cat.isActive());
        m.put("parentId", cat.getParent() != null ? cat.getParent().getId().toString() : "");
        return m;
    }

    // ---- private helpers ----

    private Category findById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    // Nếu request có slug thì dùng, không thì tự sinh từ name
    private String resolveSlug(CategoryRequest request) {
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            return request.getSlug().trim().toLowerCase();
        }
        return generateSlug(request.getName());
    }

    // Chuyển tên tiếng Việt → slug: "Du lịch Biển" → "du-lich-bien"
    // Bước 1: Normalize NFD — tách ký tự gốc và dấu thành 2 phần riêng (VD: ị → i + combining dot)
    // Bước 2: Xóa dấu (các combining character có block InCombiningDiacriticalMarks)
    // Bước 3: Xóa ký tự đặc biệt, chỉ giữ chữ và số
    // Bước 4: Thay khoảng trắng bằng dấu gạch ngang, trim đầu cuối
    private String generateSlug(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d").replaceAll("Đ", "D")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }

    // Nếu parentId null → category gốc, có parentId → tìm parent và validate tồn tại
    private Category resolveParent(UUID parentId) {
        if (parentId == null) return null;
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Category (parent)", parentId));
    }
}
