package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.PaymentResponse;
import com.sun.bookingtours.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    // booking.id nằm trong nested object → chỉ tường minh cho MapStruct
    @Mapping(source = "booking.id", target = "bookingId")
    PaymentResponse toResponse(Payment payment);
}
