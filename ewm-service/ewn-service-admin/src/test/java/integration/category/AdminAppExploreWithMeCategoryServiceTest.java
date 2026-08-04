package integration.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.category.*;
import ru.practicum.exceptions.DuplicatedDataException;
import ru.practicum.exceptions.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = AdminAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class AdminAppExploreWithMeCategoryServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryAdminService categoryAdminService;

    private CategoryDto createCategoryDto(String name) {
        NewCategoryDto newCategoryDto = NewCategoryDto.builder()
                .name(name)
                .build();

        return categoryAdminService.addCategory(newCategoryDto);
    }

    @Test
    void addCategory_Success() {
        CategoryDto dto = createCategoryDto("Концерты");

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getName()).isEqualTo("Концерты");
        assertThat(categoryRepository.findById(dto.getId())).isPresent();
    }

    @Test
    void addCategory_DuplicateName_ThrowsException() {
        createCategoryDto("Концерты");

        NewCategoryDto duplicate = NewCategoryDto.builder()
                .name("Концерты")
                .build();

        assertThatThrownBy(() -> categoryAdminService.addCategory(duplicate))
                .isInstanceOf(DuplicatedDataException.class)
                .hasMessage("Категория с таким названием уже существует");
    }

    @Test
    void deleteCategory_Success() {
        CategoryDto dto = createCategoryDto("Фестивали");

        categoryAdminService.deleteCat(dto.getId());

        assertThat(categoryRepository.findById(dto.getId())).isEmpty();
    }

    @Test
    void deleteCategory_NotFound_ThrowsException() {
        assertThatThrownBy(() -> categoryAdminService.deleteCat(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Категория не найдена по id = 999");
    }

    @Test
    void updateCategory_Success() {
        CategoryDto dto = createCategoryDto("Спорт");

        NewCategoryDto updateDto = NewCategoryDto.builder()
                .name("Футбол")
                .build();

        CategoryDto updated = categoryAdminService.updateCat(dto.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Футбол");
        assertThat(categoryRepository.findById(dto.getId())).get().extracting(Category::getName).isEqualTo("Футбол");
    }

    @Test
    void updateCategory_DuplicateName_ThrowsException() {
        createCategoryDto("Музыка");
        CategoryDto dto2 = createCategoryDto("Театр");

        NewCategoryDto updateDto = NewCategoryDto.builder()
                .name("Музыка")
                .build();

        assertThatThrownBy(() -> categoryAdminService.updateCat(dto2.getId(), updateDto))
                .isInstanceOf(DuplicatedDataException.class)
                .hasMessage("Категория с таким названием уже существует");
    }

    @Test
    void updateCategory_NotFound_ThrowsException() {
        NewCategoryDto updateDto = NewCategoryDto.builder()
                .name("Фильмы")
                .build();

        assertThatThrownBy(() -> categoryAdminService.updateCat(999L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Категория не найдена по id = 999");
    }
}
