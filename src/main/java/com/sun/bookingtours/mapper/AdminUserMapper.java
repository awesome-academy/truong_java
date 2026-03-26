package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.AdminUserResponse;
import com.sun.bookingtours.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    // source = "active": Lombok generate isActive() → MapStruct đọc property tên "active"
    @Mapping(source = "active", target = "isActive")
    AdminUserResponse toResponse(User user);
}
