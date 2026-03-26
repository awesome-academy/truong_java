package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.AdminBookingDetailResponse;
import com.sun.bookingtours.dto.response.AdminBookingResponse;
import com.sun.bookingtours.dto.response.PaymentResponse;
import com.sun.bookingtours.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminBookingMapper {

    @Mapping(source = "user.id",                target = "userId")
    @Mapping(source = "user.fullName",           target = "userFullName")
    @Mapping(source = "user.email",              target = "userEmail")
    @Mapping(source = "schedule.tour.title",     target = "tourTitle")
    @Mapping(source = "schedule.departureDate",  target = "departureDate")
    AdminBookingResponse toResponse(Booking booking);

    // MapStruct hỗ trợ multi-source: tham số thứ 2 (payment) được map trực tiếp vào field "payment"
    // booking.id / booking.status phải khai báo tường minh vì PaymentResponse cũng có id, status → ambiguous
    @Mapping(source = "booking.id",                  target = "id")
    @Mapping(source = "booking.bookingCode",          target = "bookingCode")
    @Mapping(source = "booking.status",              target = "status")
    @Mapping(source = "booking.numParticipants",     target = "numParticipants")
    @Mapping(source = "booking.totalAmount",         target = "totalAmount")
    @Mapping(source = "booking.bookedAt",            target = "bookedAt")
    @Mapping(source = "booking.cancelledAt",         target = "cancelledAt")
    @Mapping(source = "booking.cancelReason",        target = "cancelReason")
    @Mapping(source = "booking.user.id",             target = "userId")
    @Mapping(source = "booking.user.fullName",        target = "userFullName")
    @Mapping(source = "booking.user.email",           target = "userEmail")
    @Mapping(source = "booking.user.phone",           target = "userPhone")
    @Mapping(source = "booking.schedule.id",          target = "scheduleId")
    @Mapping(source = "booking.schedule.tour.title",  target = "tourTitle")
    @Mapping(source = "booking.schedule.tour.slug",   target = "tourSlug")
    @Mapping(source = "booking.schedule.departureDate", target = "departureDate")
    @Mapping(source = "payment",                      target = "payment")
    AdminBookingDetailResponse toDetailResponse(Booking booking, PaymentResponse payment);
}
