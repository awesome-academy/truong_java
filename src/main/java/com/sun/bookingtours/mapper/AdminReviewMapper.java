package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.AdminReviewResponse;
import com.sun.bookingtours.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminReviewMapper {

    @Mapping(source = "user.id",        target = "userId")
    @Mapping(source = "user.fullName",  target = "userFullName")
    @Mapping(source = "user.email",     target = "userEmail")
    @Mapping(source = "approved",       target = "isApproved")
    AdminReviewResponse toResponse(Review review);
}
