package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exceptions.NotFoundException;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryPublicServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @Override
    public Collection<CategoryDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        List<Category> categoryList = categoryRepository.findAll(pageable).getContent();

        return categoryList.stream()
                .map(mapper::mapToCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long catId) {
        Category find = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена по id = " + catId));

        return mapper.mapToCategoryDto(find);
    }
}
