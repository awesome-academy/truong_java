package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.BankAccountResponse;
import com.sun.bookingtours.dto.response.UserProfileResponse;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.entity.UserBankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// componentModel = "spring": MapStruct generate ra UserMapperImpl và đăng ký như Spring Bean
// → có thể inject vào Service bằng @RequiredArgsConstructor như bình thường
@Mapper(componentModel = "spring")
public interface UserMapper {

    // MapStruct tự map field cùng tên: user.fullName → response.fullName, user.email → response.email...
    // Field không có trong UserProfileResponse (passwordHash, refreshToken) → tự động bỏ qua
    // source = "active": MapStruct đọc getter isActive() của User → property tên "active"
    // target = "isActive": map vào field isActive trong UserProfileResponse
    @Mapping(source = "active", target = "isActive")
    UserProfileResponse toProfileResponse(User user);

    // Map UserBankAccount entity → BankAccountResponse DTO
    // Field "user" trong entity không có trong DTO → tự động bỏ qua
    BankAccountResponse toBankAccountResponse(UserBankAccount bankAccount);
}
