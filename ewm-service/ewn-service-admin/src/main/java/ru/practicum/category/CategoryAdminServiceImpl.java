package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.DuplicatedDataException;
import ru.practicum.exceptions.NotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryAdminServiceImpl implements CategoryAdminService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new DuplicatedDataException("Категория с таким названием уже существует");
        }

        Category category = mapper.mapToCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);

        return mapper.mapToCategoryDto(savedCategory);

    }

    @Override
    public void deleteCat(Long catId) {
        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена по id = " + catId));

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Существуют события, связанные с этой категорией");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCat(Long catId, NewCategoryDto newCategoryDto) {
        Category updated = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена по id = " + catId));

        if (newCategoryDto.getName() != null && categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new DuplicatedDataException("Категория с таким названием уже существует");
        }

        if (newCategoryDto.getName() != null) {
            updated.setName(newCategoryDto.getName());
        }

        Category savedCategory = categoryRepository.save(updated);
        return mapper.mapToCategoryDto(savedCategory);

    }
}
