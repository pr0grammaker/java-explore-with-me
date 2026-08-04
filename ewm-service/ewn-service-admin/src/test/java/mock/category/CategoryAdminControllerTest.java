package mock.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.category.CategoryAdminController;
import ru.practicum.category.CategoryAdminService;
import ru.practicum.category.CategoryDto;
import ru.practicum.category.NewCategoryDto;
import ru.practicum.exceptions.DuplicatedDataException;
import ru.practicum.exceptions.NotFoundException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryAdminController.class)
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class CategoryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryAdminService categoryAdminService;

    @Autowired
    private ObjectMapper objectMapper;

    private NewCategoryDto newCategoryDto;
    private CategoryDto categoryDto;

    @BeforeEach
    void setup() {
        newCategoryDto = NewCategoryDto.builder()
                .name("Концерты")
                .build();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();
    }

    @Test
    void addCategory_ReturnsCreatedCategory() throws Exception {
        when(categoryAdminService.addCategory(any(NewCategoryDto.class)))
                .thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Концерты"));

        verify(categoryAdminService, times(1)).addCategory(any(NewCategoryDto.class));
    }

    @Test
    void addCategory_DuplicateName_ReturnsConflict() throws Exception {
        doThrow(new DuplicatedDataException("Категория с таким названием уже существует"))
                .when(categoryAdminService).addCategory(any(NewCategoryDto.class));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Категория с таким названием уже существует"));

        verify(categoryAdminService, times(1)).addCategory(any(NewCategoryDto.class));
    }

    @Test
    void deleteCategory_ReturnsNoContent() throws Exception {
        doNothing().when(categoryAdminService).deleteCat(1L);

        mockMvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isNoContent());

        verify(categoryAdminService, times(1)).deleteCat(1L);
    }

    @Test
    void deleteCategory_NotFound_Returns404() throws Exception {
        doThrow(new NotFoundException("Категория не найдена по id = 999"))
                .when(categoryAdminService).deleteCat(999L);

        mockMvc.perform(delete("/admin/categories/{catId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Категория не найдена по id = 999"));

        verify(categoryAdminService, times(1)).deleteCat(999L);
    }

    @Test
    void updateCategory_ReturnsUpdatedCategory() throws Exception {
        NewCategoryDto updateDto = NewCategoryDto.builder()
                .name("Фестивали")
                .build();

        CategoryDto updatedCategory = CategoryDto.builder()
                .id(1L)
                .name("Фестивали")
                .build();

        when(categoryAdminService.updateCat(eq(1L), any(NewCategoryDto.class)))
                .thenReturn(updatedCategory);

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Фестивали"));
    }

    @Test
    void updateCategory_DuplicateName_ReturnsConflict() throws Exception {
        NewCategoryDto updateDto = NewCategoryDto.builder()
                .name("Концерты")
                .build();

        doThrow(new DuplicatedDataException("Категория с таким названием уже существует"))
                .when(categoryAdminService).updateCat(eq(1L), any(NewCategoryDto.class));

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Категория с таким названием уже существует"));
    }

    @Test
    void updateCategory_NotFound_Returns404() throws Exception {
        NewCategoryDto updateDto = NewCategoryDto.builder()
                .name("Фестивали")
                .build();

        doThrow(new NotFoundException("Категория не найдена по id = 999"))
                .when(categoryAdminService).updateCat(eq(999L), any(NewCategoryDto.class));

        mockMvc.perform(patch("/admin/categories/{catId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Категория не найдена по id = 999"));
    }

    @Test
    void addCategory_BlankName_ReturnsBadRequest() throws Exception {
        NewCategoryDto dto = NewCategoryDto.builder()
                .name("")
                .build();

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCategory_NameTooLong_ReturnsBadRequest() throws Exception {
        String longName = "A".repeat(60);

        NewCategoryDto dto = NewCategoryDto.builder()
                .name(longName)
                .build();

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Название категории должно быть от 1 до 50 символов"));
    }

}
