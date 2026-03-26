package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.FoodResponse;
import com.sun.bookingtours.entity.Food;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    FoodResponse toResponse(Food food);
}
