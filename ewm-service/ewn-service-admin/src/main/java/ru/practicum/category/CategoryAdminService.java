package ru.practicum.category;

public interface CategoryAdminService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    void deleteCat(Long catId);

    CategoryDto updateCat(Long catId, NewCategoryDto newCategoryDto);
}
