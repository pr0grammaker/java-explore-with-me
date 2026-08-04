package mock.event;

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
import ru.practicum.category.CategoryDto;
import ru.practicum.event.*;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventAdminController.class)
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class EventAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventAdminService eventAdminService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void getEvents_ReturnsEvents() throws Exception {
        when(eventAdminService.getEvents(
                List.of(5), List.of("PUBLISHED"), List.of(10),
                "2024-01-01 00:00:00", "2024-12-31 23:59:59", 0, 10))
                .thenReturn(List.of(eventFullDto));

        mockMvc.perform(get("/admin/events")
                        .param("users", "5")
                        .param("states", "PUBLISHED")
                        .param("categories", "10")
                        .param("rangeStart", "2024-01-01 00:00:00")
                        .param("rangeEnd", "2024-12-31 23:59:59")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Знаменитое шоу 'Летающая кукуруза'"))
                .andExpect(jsonPath("$[0].initiator.name").value("Иван Иванов"))
                .andExpect(jsonPath("$[0].category.name").value("Концерт"));
    }

    @Test
    void getEvents_EmptyResult() throws Exception {
        when(eventAdminService.getEvents(
                List.of(99), List.of("PUBLISHED"), List.of(99),
                "2024-01-01 00:00:00", "2024-12-31 23:59:59", 0, 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/events")
                        .param("users", "99")
                        .param("states", "PUBLISHED")
                        .param("categories", "99")
                        .param("rangeStart", "2024-01-01 00:00:00")
                        .param("rangeEnd", "2024-12-31 23:59:59")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void updateEvent_Success() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .title("Новое название")
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();

        eventFullDto.setTitle(request.getTitle());

        when(eventAdminService.updateEvent(1L, request)).thenReturn(eventFullDto);

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Новое название"));
    }

    @Test
    void updateEvent_NotFound() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .title("Новое название")
                .build();

        when(eventAdminService.updateEvent(999L, request))
                .thenThrow(new NotFoundException("Событие по id=999 не найдено"));

        mockMvc.perform(patch("/admin/events/{eventId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Событие по id=999 не найдено"));
    }

    @Test
    void updateEvent_InvalidStateForPublish() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();

        when(eventAdminService.updateEvent(1L, request))
                .thenThrow(new InvalidEventOperationException(
                        "Событие можно публиковать только если оно в состоянии ожидания публикации"));

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Событие можно публиковать только если оно в состоянии ожидания публикации"));
    }

    @Test
    void updateEvent_InvalidEventDate() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .eventDate(LocalDateTime.now().plusMinutes(30))
                .build();

        when(eventAdminService.updateEvent(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenThrow(new InvalidEventOperationException(
                        "Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"));

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"));
    }

    @Test
    void updateEvent_RejectEvent_Success() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.REJECT_EVENT)
                .build();

        EventFullDto canceledEvent = EventFullDto.builder()
                .id(1L)
                .state(EventState.CANCELED)
                .title("Событие отклонено")
                .build();

        when(eventAdminService.updateEvent(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenReturn(canceledEvent);

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("CANCELED"));
    }

    @Test
    void updateEvent_RejectEvent_Published() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.REJECT_EVENT)
                .build();

        when(eventAdminService.updateEvent(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenThrow(new InvalidEventOperationException("Событие нельзя отклонить, если оно уже опубликовано"));

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Событие нельзя отклонить, если оно уже опубликовано"));
    }

    @Test
    void updateEvent_CategoryFound() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .category(10L)
                .build();

        when(eventAdminService.updateEvent(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category.id").value(10));
    }

    @Test
    void updateEvent_CategoryNotFound() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .category(999L)
                .build();

        when(eventAdminService.updateEvent(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenThrow(new NotFoundException("Категория с id = 999 не найдена"));

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Категория с id = 999 не найдена"));
    }

    @Test
    void updateEvent_LocationConflict() throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .title("Новое название")
                .build();

        when(eventAdminService.updateEvent(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenThrow(new InvalidEventOperationException("На указанной локации уже запланировано событие на это время"));

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("На указанной локации уже запланировано событие на это время"));
    }



}
