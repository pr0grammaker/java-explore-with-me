package mock.event;

import jakarta.servlet.http.HttpServletRequest;
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
import ru.practicum.event.*;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventPublicController.class)
@ContextConfiguration(classes = PublicAppExploreWithMe.class)
public class EventPublicControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventPublicService eventPublicService;

    private EventFullDto eventFullDto;

    @BeforeEach
    public void setup() {
        CategoryDto category = CategoryDto.builder()
                .id(10L)
                .name("Концерт")
                .build();

        UserShortDto initiator = UserShortDto.builder()
                .id(5L)
                .name("Иван Иванов")
                .build();

        Location location = Location.builder()
                .lat(51.1694f)
                .lon(71.4491f)
                .build();

        eventFullDto = EventFullDto.builder()
                .id(1L)
                .title("Знаменитое шоу 'Летающая кукуруза'")
                .annotation("Эксклюзивность нашего шоу гарантирует привлечение максимальной зрительской аудитории")
                .description("Что получится, если соединить кукурузу и полёт? Создатели шоу испытали эту идею на практике...")
                .category(category)
                .initiator(initiator)
                .location(location)
                .eventDate(LocalDateTime.of(2024, 12, 31, 15, 10, 5))
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .confirmedRequests(5L)
                .views(999L)
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .build();
    }

    @Test
    void getAllEvents_ReturnsList() throws Exception {
        when(eventPublicService.getAllEvents(
                anyString(), anyList(), anyBoolean(),
                anyString(), anyString(), anyBoolean(),
                anyString(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                .thenReturn(List.of(eventFullDto));

        mockMvc.perform(get("/events")
                        .param("text", "кукуруза")
                        .param("categories", "10")
                        .param("paid", "true")
                        .param("rangeStart", "2024-12-01 00:00:00")
                        .param("rangeEnd", "2024-12-31 23:59:59")
                        .param("onlyAvailable", "false")
                        .param("sort", "EVENT_DATE")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Знаменитое шоу 'Летающая кукуруза'"))
                .andExpect(jsonPath("$[0].category.name").value("Концерт"))
                .andExpect(jsonPath("$[0].initiator.name").value("Иван Иванов"));

        verify(eventPublicService, times(1)).getAllEvents(
                anyString(), anyList(), anyBoolean(),
                anyString(), anyString(), anyBoolean(),
                anyString(), anyInt(), anyInt(), any(HttpServletRequest.class));
    }

    @Test
    void getEventById_ReturnsEvent() throws Exception {
        when(eventPublicService.getEventById(eq(1L), any(HttpServletRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/events/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Знаменитое шоу 'Летающая кукуруза'"))
                .andExpect(jsonPath("$.category.name").value("Концерт"))
                .andExpect(jsonPath("$.initiator.name").value("Иван Иванов"));

        verify(eventPublicService, times(1)).getEventById(eq(1L), any(HttpServletRequest.class));
    }

    @Test
    void getEventById_NotFound_Returns404() throws Exception {
        doThrow(new NotFoundException("Событие по id=999 не найдено"))
                .when(eventPublicService).getEventById(eq(999L), any(HttpServletRequest.class));

        mockMvc.perform(get("/events/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Событие по id=999 не найдено"));

        verify(eventPublicService, times(1)).getEventById(eq(999L), any(HttpServletRequest.class));
    }
}
