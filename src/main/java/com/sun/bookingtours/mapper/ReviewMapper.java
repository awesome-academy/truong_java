package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.ReviewResponse;
import com.sun.bookingtours.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    // source = "user.id" → MapStruct sinh: response.userId = review.getUser().getId()
    // source = "booking.id" → booking có thể null → MapStruct tự handle null-safe (trả null)
    // source = "approved" → Lombok sinh getter isApproved() cho boolean field → MapStruct strip prefix "is" → thấy property tên "approved"
    //                        target = "isApproved" vì record field tên isApproved
    @Mapping(source = "user.id",    target = "userId")
    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "approved",   target = "isApproved")
    ReviewResponse toResponse(Review review);
}
