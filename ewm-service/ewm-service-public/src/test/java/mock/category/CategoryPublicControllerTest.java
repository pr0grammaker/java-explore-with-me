package mock.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.PublicAppExploreWithMe;
import ru.practicum.category.CategoryDto;
import ru.practicum.category.CategoryPublicController;
import ru.practicum.category.CategoryService;
import ru.practicum.exceptions.NotFoundException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryPublicController.class)
@ContextConfiguration(classes = PublicAppExploreWithMe.class)
public class CategoryPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;


    private CategoryDto categoryDto;

    @BeforeEach
    void setup() {
        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();
    }

    @Test
    void getAllCategories_ReturnsList() throws Exception {
        when(categoryService.getAllCategories(0, 10))
                .thenReturn(List.of(categoryDto));

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Концерты"));

        verify(categoryService, times(1)).getAllCategories(0, 10);
    }

    @Test
    void getById_ReturnsCategory() throws Exception {
        when(categoryService.getById(1L)).thenReturn(categoryDto);

        mockMvc.perform(get("/categories/{catId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Концерты"));

        verify(categoryService, times(1)).getById(1L);
    }

    @Test
    void getById_NotFound_Returns404() throws Exception {
        doThrow(new NotFoundException("Категория не найдена по id = 999"))
                .when(categoryService).getById(999L);

        mockMvc.perform(get("/categories/{catId}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Категория не найдена по id = 999"));

        verify(categoryService, times(1)).getById(999L);
    }


}
