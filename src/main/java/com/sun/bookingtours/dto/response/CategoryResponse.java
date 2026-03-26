package com.sun.bookingtours.dto.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CategoryResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private boolean isActive;

    // Danh sách category con — tạo thành cấu trúc cây (tree)
    // Lý do dùng List<CategoryResponse> (đệ quy): mỗi node con cũng có thể có children của nó
    // VD: Miền Bắc → [Hà Nội → [Hoàn Kiếm, Ba Đình], Hải Phòng]
    private List<CategoryResponse> children;
}
