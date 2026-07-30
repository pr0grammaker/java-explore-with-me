package ru.practicum.category;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    Category mapToCategory(NewCategoryDto newCategoryDto);

    CategoryDto mapToCategoryDto(Category savedCategory);
}
