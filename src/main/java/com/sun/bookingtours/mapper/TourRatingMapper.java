package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.TourRatingResponse;
import com.sun.bookingtours.entity.TourRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TourRatingMapper {

    @Mapping(source = "tour.id", target = "tourId")
    TourRatingResponse toResponse(TourRating rating);
}
