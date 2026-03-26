package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.CategoryResponse;
import com.sun.bookingtours.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // MapStruct không tự build tree được vì entity không có field children
    // → chỉ map các field cơ bản, Service sẽ tự gắn children vào sau
    // isActive: cả entity lẫn DTO đều là boolean → getter isActive() → property "active" ở cả 2 bên
    // MapStruct tự map được, không cần @Mapping
    @Mapping(target = "children", ignore = true) // Service xử lý
    CategoryResponse toResponse(Category category);
}
