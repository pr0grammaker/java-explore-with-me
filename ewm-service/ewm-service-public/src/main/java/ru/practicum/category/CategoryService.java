package ru.practicum.category;

import java.util.Collection;

public interface CategoryService {
    Collection<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getById(Long catId);
}
