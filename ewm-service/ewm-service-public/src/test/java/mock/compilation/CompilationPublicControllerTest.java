package mock.compilation;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import ru.practicum.PublicAppExploreWithMe;
import ru.practicum.compilation.CompilationDto;
import ru.practicum.compilation.CompilationPublicController;
import ru.practicum.compilation.CompilationPublicService;
import ru.practicum.event.EventCompilationDto;
import ru.practicum.exceptions.NotFoundException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CompilationPublicController.class)
@ContextConfiguration(classes = PublicAppExploreWithMe.class)
public class CompilationPublicControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationPublicService compilationPublicService;


    private CompilationDto compilationDto;

    @BeforeEach
    void setup() {
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
    void getAllCompilations_ReturnsList() throws Exception {
        when(compilationPublicService.getAllCompilations(true, 0, 10))
                .thenReturn(List.of(compilationDto));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Летние концерты"))
                .andExpect(jsonPath("$[0].pinned").value(true))
                .andExpect(jsonPath("$[0].events[0].title").value("Событие 1"));

        verify(compilationPublicService, times(1)).getAllCompilations(true, 0, 10);
    }

    @Test
    void getCompilationById_ReturnsCompilation() throws Exception {
        when(compilationPublicService.getCompilationById(1L)).thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/{compId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Летние концерты"))
                .andExpect(jsonPath("$.pinned").value(true))
                .andExpect(jsonPath("$.events[0].title").value("Событие 1"));

        verify(compilationPublicService, times(1)).getCompilationById(1L);
    }

    @Test
    void getCompilationById_NotFound_Returns404() throws Exception {
        doThrow(new NotFoundException("Подборка с id=999 не найдена"))
                .when(compilationPublicService).getCompilationById(999L);

        mockMvc.perform(get("/compilations/{compId}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Подборка с id=999 не найдена"));

        verify(compilationPublicService, times(1)).getCompilationById(999L);


    }
}
