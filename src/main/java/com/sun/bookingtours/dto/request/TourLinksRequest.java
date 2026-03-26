package com.sun.bookingtours.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TourLinksRequest {

    // Danh sách UUID của places hoặc foods cần liên kết với tour
    // Gửi list rỗng = xóa hết liên kết hiện tại
    @NotNull
    private List<UUID> ids;
}
