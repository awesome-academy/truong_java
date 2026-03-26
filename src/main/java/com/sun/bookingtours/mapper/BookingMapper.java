package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.BookingResponse;
import com.sun.bookingtours.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    // MapStruct không tự biết "scheduleId" lấy từ đâu vì tên khác với field trong entity
    // @Mapping(source = "schedule.id", target = "scheduleId") → chỉ tường minh: lấy booking.schedule.id
    // MapStruct tự sinh: response.scheduleId = booking.getSchedule().getId()
    @Mapping(source = "schedule.id",              target = "scheduleId")
    @Mapping(source = "schedule.tour.title",       target = "tourTitle")
    @Mapping(source = "schedule.departureDate",    target = "departureDate")
    BookingResponse toResponse(Booking booking);
}
