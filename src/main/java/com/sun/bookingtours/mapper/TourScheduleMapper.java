package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.TourScheduleResponse;
import com.sun.bookingtours.entity.TourSchedule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TourScheduleMapper {

    TourScheduleResponse toResponse(TourSchedule schedule);
}
