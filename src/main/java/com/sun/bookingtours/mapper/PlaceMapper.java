package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.PlaceResponse;
import com.sun.bookingtours.entity.Place;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlaceMapper {

    PlaceResponse toResponse(Place place);
}
