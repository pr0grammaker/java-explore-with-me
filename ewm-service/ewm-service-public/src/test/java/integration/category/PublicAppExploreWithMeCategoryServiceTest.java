package integration.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.PublicAppExploreWithMe;
import ru.practicum.category.*;
import ru.practicum.exceptions.NotFoundException;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PublicAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class PublicAppExploreWithMeCategoryServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    private CategoryDto createCategoryDto(String name) {
        Category category = Category.builder()
                .name(name)
                .build();

        categoryRepository.save(category);

        return categoryMapper.mapToCategoryDto(category);
    }

    @Test
    void getAllCategories_ReturnsPagedList() {
        createCategoryDto("Концерты");
        createCategoryDto("Фестивали");
        createCategoryDto("Спорт");

        Collection<CategoryDto> categories = categoryService.getAllCategories(0, 2);

        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(CategoryDto::getName)
                .containsExactly("Концерты", "Фестивали");
    }

    @Test
    void getAllCategories_SecondPage() {
        createCategoryDto("Концерты");
        createCategoryDto("Фестивали");
        createCategoryDto("Спорт");

        Collection<CategoryDto> categories = categoryService.getAllCategories(2, 2);

        assertThat(categories).hasSize(1);
        assertThat(categories.iterator().next().getName()).isEqualTo("Спорт");
    }

    @Test
    void getById_ReturnsCategory() {
        CategoryDto dto = createCategoryDto("Музыка");

        CategoryDto found = categoryService.getById(dto.getId());

        assertThat(found.getId()).isEqualTo(dto.getId());
        assertThat(found.getName()).isEqualTo("Музыка");
    }

    @Test
    void getById_NotFound_ThrowsException() {
        assertThatThrownBy(() -> categoryService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Категория не найдена по id = 999");
    }


}
