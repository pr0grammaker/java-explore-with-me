package mock.compilation;

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
import ru.practicum.compilation.*;
import ru.practicum.event.EventCompilationDto;
import ru.practicum.exceptions.NotFoundException;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompilationAdminController.class)
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class CompilationAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationAdminService compilationAdminService;

    @Autowired
    private ObjectMapper objectMapper;

    private NewCompilationDto newCompilationDto;
    private CompilationDto compilationDto;

    @BeforeEach
    void setup() {
        newCompilationDto = NewCompilationDto.builder()
                .events(List.of(1L, 2L))
                .pinned(true)
                .title("Летние концерты")
                .build();

        compilationDto = CompilationDto.builder()
                .id(1L)
                .title("Летние концерты")
                .pinned(true)
                .events(List.of(
                        EventCompilationDto.builder()
                                .id(1L)
                                .title("Событие 1")
                                .annotation("Аннотация")
                                .paid(true)
                                .views(100L)
                                .confirmedRequests(5L)
                                .build()
                ))
                .build();
    }

    @Test
    void addCompilation_ReturnsCreatedCompilation() throws Exception {
        when(compilationAdminService.addCompilation(any(NewCompilationDto.class)))
                .thenReturn(compilationDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Летние концерты"))
                .andExpect(jsonPath("$.pinned").value(true))
                .andExpect(jsonPath("$.events[0].title").value("Событие 1"));
    }

    @Test
    void deleteCompilation_ReturnsNoContent() throws Exception {
        doNothing().when(compilationAdminService).deleteCompilation(1L);

        mockMvc.perform(delete("/admin/compilations/{compId}", 1L))
                .andExpect(status().isNoContent());

        verify(compilationAdminService, times(1)).deleteCompilation(1L);
    }

    @Test
    void updateCompilation_ReturnsUpdatedCompilation() throws Exception {
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("Обновленная подборка")
                .pinned(false)
                .build();

        CompilationDto updatedDto = CompilationDto.builder()
                .id(1L)
                .title("Обновленная подборка")
                .pinned(false)
                .events(Collections.emptyList())
                .build();

        when(compilationAdminService.updateCompilation(eq(1L), any(UpdateCompilationRequest.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/admin/compilations/{compId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Обновленная подборка"))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void deleteCompilation_NotFound_Returns404() throws Exception {
        doThrow(new NotFoundException("Подборка с id=999 не найдена"))
                .when(compilationAdminService).deleteCompilation(999L);

        mockMvc.perform(delete("/admin/compilations/{compId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Подборка с id=999 не найдена"));

        verify(compilationAdminService, times(1)).deleteCompilation(999L);
    }

    @Test
    void updateCompilation_NotFound_Returns404() throws Exception {
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("Обновленная подборка")
                .pinned(false)
                .build();

        doThrow(new NotFoundException("Подборка с id=999 не найдена"))
                .when(compilationAdminService).updateCompilation(eq(999L), any(UpdateCompilationRequest.class));

        mockMvc.perform(patch("/admin/compilations/{compId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Подборка с id=999 не найдена"));
    }

    @Test
    void addCompilation_InvalidTitle_ReturnsBadRequest() throws Exception {
        NewCompilationDto dto = NewCompilationDto.builder()
                .events(List.of(1L))
                .pinned(true)
                .title("")
                .build();

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCompilation_TitleTooLong_ReturnsBadRequest() throws Exception {
        String longTitle = "A".repeat(60);

        NewCompilationDto dto = NewCompilationDto.builder()
                .events(List.of(1L))
                .pinned(true)
                .title(longTitle)
                .build();

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Заголовок должен содержать от 1 до 50 символов"));
    }

    @Test
    void addCompilation_NullPinned_ReturnsBadRequest() throws Exception {
        NewCompilationDto dto = NewCompilationDto.builder()
                .events(List.of(1L))
                .pinned(null)
                .title("Концерты")
                .build();

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Статус подборки обязателен"));
    }

    @Test
    void addCompilation_InvalidEvents_ReturnsBadRequest() throws Exception {
        NewCompilationDto dto = NewCompilationDto.builder()
                .events(List.of(-1L))
                .pinned(true)
                .title("Концерты")
                .build();

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCompilation_InvalidEvents_ReturnsBadRequest() throws Exception {
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .events(List.of(-1L)) // отрицательный id
                .pinned(true)
                .title("Концерты")
                .build();

        mockMvc.perform(patch("/admin/compilations/{compId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCompilation_TitleTooLong_ReturnsBadRequest() throws Exception {
        String longTitle = "A".repeat(60);

        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .events(List.of(1L))
                .pinned(true)
                .title(longTitle)
                .build();

        mockMvc.perform(patch("/admin/compilations/{compId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Заголовок должен содержать от 1 до 50 символов"));
    }

}
