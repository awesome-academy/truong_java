package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TourImageRequest {

    // Admin gửi lên danh sách URL ảnh đã upload (hoặc URL ngoài)
    // Khi tích hợp UI sau: UI upload file → nhận URL → truyền vào đây
    // Approach này tách biệt "upload file" khỏi "gắn ảnh vào tour"
    @NotEmpty
    private List<String> imageUrls;
}
