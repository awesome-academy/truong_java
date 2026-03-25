package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.TourImageResponse;
import com.sun.bookingtours.dto.response.TourResponse;
import com.sun.bookingtours.entity.Food;
import com.sun.bookingtours.entity.Place;
import com.sun.bookingtours.entity.Tour;
import com.sun.bookingtours.entity.TourImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TourMapper {

    // category → category.id, category.name, category.slug
    @Mapping(target = "category.id",   source = "category.id")
    @Mapping(target = "category.name", source = "category.name")
    @Mapping(target = "category.slug", source = "category.slug")
    // places/foods: List<Place>/List<Food> → List<LinkSummary>
    // MapStruct dùng method mapPlace/mapFood bên dưới để convert từng phần tử
    @Mapping(target = "places", source = "places")
    @Mapping(target = "foods",  source = "foods")
    TourResponse toResponse(Tour tour);

    TourImageResponse toImageResponse(TourImage image);

    @Mapping(target = "id",   source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "slug", source = "slug")
    TourResponse.LinkSummary mapPlace(Place place);

    @Mapping(target = "id",   source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "slug", source = "slug")
    TourResponse.LinkSummary mapFood(Food food);
}
