package com.sun.bookingtours.mapper;

import com.sun.bookingtours.dto.response.NewsResponse;
import com.sun.bookingtours.entity.News;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NewsMapper {

    // MapStruct không tự map nested object sang flat field
    // → dùng @Mapping để chỉ rõ: lấy author.id → authorId, author.fullName → authorName
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.fullName", target = "authorName")
    NewsResponse toResponse(News news);
}
