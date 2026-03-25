package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // Lấy tất cả category gốc (không có cha) đang active
    // Dùng để build tree: lấy roots trước, sau đó đệ quy children
    List<Category> findByParentIsNullAndIsActiveTrue();

    // Lấy children của 1 category cha, chỉ lấy active
    // Dùng khi cần lazy-load từng tầng con
    List<Category> findByParentIdAndIsActiveTrue(UUID parentId);

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // Lấy toàn bộ category active (flat list) — Service sẽ tự build thành tree
    // Lý do dùng @Query + JOIN FETCH: tránh N+1 query khi JPA load từng parent riêng lẻ
    // JOIN FETCH parent nghĩa là: load category và parent của nó trong 1 câu SQL duy nhất
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.isActive = true")
    List<Category> findAllActiveWithParent();
}
