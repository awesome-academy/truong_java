package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryRequest {

    @NotBlank
    private String name;

    // Slug là URL-friendly identifier, VD: "du-lich-bien"
    // Nếu không truyền lên, Service sẽ tự sinh từ name
    // @Pattern chỉ áp dụng khi slug không null
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    private String slug;

    private String description;

    private String imageUrl;

    // null = category gốc (root), có giá trị = category con
    private UUID parentId;
}
