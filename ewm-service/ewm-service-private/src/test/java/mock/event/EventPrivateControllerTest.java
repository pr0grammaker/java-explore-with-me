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
import ru.practicum.PrivateAppExploreWithMe;
import ru.practicum.category.CategoryDto;
import ru.practicum.event.*;
import ru.practicum.exceptions.ConditionsNotMetException;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.participationrequest.ParticipationRequestDto;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(EventController.class)
@ContextConfiguration(classes = PrivateAppExploreWithMe.class)
public class EventPrivateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private EventFullDto eventFullDto;
    private EventShortDto eventShortDto;
    private ParticipationRequestDto participationRequestDto;
    private UpdateEventUserRequest updateEventUserRequest;
    private EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest;

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
                .eventDate(LocalDateTime.of(2027, 12, 31, 15, 10, 5))
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .confirmedRequests(5L)
                .views(999L)
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .build();

        eventShortDto = EventShortDto.builder()
                .annotation("Эксклюзивность нашего шоу гарантирует привлечение максимальной зрительской аудитории")
                .description("Что получится, если соединить кукурузу и полёт? Создатели шоу испытали эту идею на практике...")
                .title("Знаменитое шоу 'Летающая кукуруза'")
                .category(10L)
                .eventDate(LocalDateTime.of(2027, 12, 31, 15, 10, 5))
                .location(location)
                .paid(true)
                .participantLimit(100L)
                .requestModeration(true)
                .build();

        updateEventUserRequest = UpdateEventUserRequest.builder()
                .annotation("Обновлённая аннотация")
                .description("Обновлённое описание")
                .title("Обновлённый заголовок")
                .category(10L)
                .eventDate(LocalDateTime.of(2027, 12, 31, 18, 0, 0))
                .location(Location.builder().lat(51.1694f).lon(71.4491f).build())
                .paid(true)
                .participantLimit(50L)
                .requestModeration(true)
                .stateAction(StateAction.SEND_TO_REVIEW)
                .build();

        eventRequestStatusUpdateRequest = new EventRequestStatusUpdateRequest();
        eventRequestStatusUpdateRequest.setRequestIds(List.of(100L));
        eventRequestStatusUpdateRequest.setStatus(RequestStatus.CONFIRMED);

        participationRequestDto = ParticipationRequestDto.builder()
                .id(100L)
                .event(1L)
                .requester(20L)
                .status(String.valueOf(RequestStatus.CONFIRMED))
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void getEventsByUser_ReturnsEvents() throws Exception {
        Long userId = 5L;
        int from = 0;
        int size = 10;

        List<EventFullDto> events = List.of(eventFullDto);

        when(eventService.getEventsByUser(userId, from, size)).thenReturn(events);

        mockMvc.perform(get("/users/{userId}/events", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventFullDto.getTitle()))
                .andExpect(jsonPath("$[0].annotation").value(eventFullDto.getAnnotation()))
                .andExpect(jsonPath("$[0].category.id").value(eventFullDto.getCategory().getId()))
                .andExpect(jsonPath("$[0].initiator.id").value(eventFullDto.getInitiator().getId()));

        verify(eventService, times(1)).getEventsByUser(userId, from, size);
    }

    @Test
    void addEvent_ReturnsCreatedEvent() throws Exception {
        Long userId = 5L;

        when(eventService.addEvent(eq(userId), any(EventShortDto.class))).thenReturn(eventFullDto);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()))
                .andExpect(jsonPath("$.annotation").value(eventFullDto.getAnnotation()))
                .andExpect(jsonPath("$.category.id").value(eventFullDto.getCategory().getId()))
                .andExpect(jsonPath("$.initiator.id").value(eventFullDto.getInitiator().getId()));

        verify(eventService, times(1)).addEvent(eq(userId), any(EventShortDto.class));
    }

    @Test
    void addEvent_UserNotFound_ThrowsException() throws Exception {
        Long userId = 999L;

        when(eventService.addEvent(eq(userId), any(EventShortDto.class)))
                .thenThrow(new NotFoundException("Пользователь с id = 999 не найден"));

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id = 999 не найден"));

        verify(eventService, times(1)).addEvent(eq(userId), any(EventShortDto.class));
    }

    @Test
    void addEvent_CategoryNotFound_ThrowsException() throws Exception {
        Long userId = 5L;

        when(eventService.addEvent(eq(userId), any(EventShortDto.class)))
                .thenThrow(new NotFoundException("Категория с id = 10 не найдена"));

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Категория с id = 10 не найдена"));

        verify(eventService, times(1)).addEvent(eq(userId), any(EventShortDto.class));
    }

    @Test
    void addEvent_EventDateTooSoon_ThrowsException() throws Exception {
        Long userId = 5L;

        when(eventService.addEvent(eq(userId), any(EventShortDto.class)))
                .thenThrow(new InvalidEventOperationException(
                        "Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"));

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"));

        verify(eventService, times(1)).addEvent(eq(userId), any(EventShortDto.class));
    }

    @Test
    void addEvent_DuplicateLocationAndDate_ThrowsException() throws Exception {
        Long userId = 5L;

        when(eventService.addEvent(eq(userId), any(EventShortDto.class)))
                .thenThrow(new InvalidEventOperationException(
                        "На указанной локации уже запланировано событие на это время"));

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("На указанной локации уже запланировано событие на это время"));

        verify(eventService, times(1)).addEvent(eq(userId), any(EventShortDto.class));
    }

    @Test
    void addEvent_BlankAnnotation_ShouldReturnBadRequest() throws Exception {
        Long userId = 5L;

        eventShortDto.setAnnotation(" ");

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addEvent_NullCategory_ShouldReturnBadRequest() throws Exception {
        Long userId = 5L;

        eventShortDto.setCategory(null);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addEvent_PastEventDate_ShouldReturnBadRequest() throws Exception {
        Long userId = 5L;

        eventShortDto.setEventDate(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Дата проведения события должна быть в будущем"));
    }

    @Test
    void addEvent_NullLocation_ShouldReturnBadRequest() throws Exception {
        Long userId = 5L;

        eventShortDto.setLocation(null);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Локация события обязательна"));
    }

    @Test
    void addEvent_NegativeParticipantLimit_ShouldReturnBadRequest() throws Exception {
        Long userId = 5L;

        eventShortDto.setParticipantLimit(-10L);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Лимит участников не может быть отрицательным"));
    }

    @Test
    void addEvent_BlankTitle_ShouldReturnBadRequest() throws Exception {
        Long userId = 5L;

        eventShortDto.setTitle(" ");

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventShortDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserEventById_ReturnsEvent() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.getUserEventById(userId, eventId)).thenReturn(eventFullDto);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()))
                .andExpect(jsonPath("$.annotation").value(eventFullDto.getAnnotation()))
                .andExpect(jsonPath("$.category.id").value(eventFullDto.getCategory().getId()))
                .andExpect(jsonPath("$.initiator.id").value(eventFullDto.getInitiator().getId()));

        verify(eventService, times(1)).getUserEventById(userId, eventId);
    }

    @Test
    void getUserEventById_UserNotFound_ThrowsException() throws Exception {
        Long userId = 999L;
        Long eventId = 1L;

        when(eventService.getUserEventById(userId, eventId))
                .thenThrow(new NotFoundException("Пользователь с id = 999 не найден"));

        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id = 999 не найден"));

        verify(eventService, times(1)).getUserEventById(userId, eventId);
    }

    @Test
    void getUserEventById_EventNotFound_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 999L;

        when(eventService.getUserEventById(userId, eventId))
                .thenThrow(new NotFoundException("Событие с id = 999 не найдено"));

        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Событие с id = 999 не найдено"));

        verify(eventService, times(1)).getUserEventById(userId, eventId);
    }

    @Test
    void getUserEventById_UserNotInitiator_ThrowsException() throws Exception {
        Long userId = 10L; // другой пользователь
        Long eventId = 1L;

        when(eventService.getUserEventById(userId, eventId))
                .thenThrow(new ConditionsNotMetException(
                        "Пользователь с id = 10 не является инициатором события id = 1"));

        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Пользователь с id = 10 не является инициатором события id = 1"));

        verify(eventService, times(1)).getUserEventById(userId, eventId);
    }

    @Test
    void updateUserEventById_ReturnsUpdatedEvent() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.updateUserEventById(eq(userId), eq(eventId), any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()))
                .andExpect(jsonPath("$.annotation").value(eventFullDto.getAnnotation()));

        verify(eventService, times(1)).updateUserEventById(eq(userId), eq(eventId), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateUserEventById_UserNotFound_ThrowsException() throws Exception {
        Long userId = 999L;
        Long eventId = 1L;

        when(eventService.updateUserEventById(eq(userId), eq(eventId), any(UpdateEventUserRequest.class)))
                .thenThrow(new NotFoundException("Пользователь с id = 999 не найден"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id = 999 не найден"));
    }

    @Test
    void updateUserEventById_EventNotFound_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 999L;

        when(eventService.updateUserEventById(eq(userId), eq(eventId), any(UpdateEventUserRequest.class)))
                .thenThrow(new NotFoundException("Событие с id = 999 не найдено"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Событие с id = 999 не найдено"));
    }

    @Test
    void updateUserEventById_UserNotInitiator_ThrowsException() throws Exception {
        Long userId = 10L;
        Long eventId = 1L;

        when(eventService.updateUserEventById(eq(userId), eq(eventId), any(UpdateEventUserRequest.class)))
                .thenThrow(new InvalidEventOperationException("Пользователь не является инициатором события"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Пользователь не является инициатором события"));
    }

    @Test
    void updateUserEventById_InvalidState_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.updateUserEventById(eq(userId), eq(eventId), any(UpdateEventUserRequest.class)))
                .thenThrow(new InvalidEventOperationException(
                        "Изменить можно только отмененные события или события в состоянии ожидания модерации"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Изменить можно только отмененные события или события в состоянии ожидания модерации"));
    }

    @Test
    void updateUserEventById_EventDateTooSoon_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.updateUserEventById(eq(userId), eq(eventId), any(UpdateEventUserRequest.class)))
                .thenThrow(new InvalidEventOperationException(
                        "Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента"));
    }

    @Test
    void updateUserEventById_DuplicateLocationAndDate_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.updateUserEventById(eq(userId), eq(eventId), any(UpdateEventUserRequest.class)))
                .thenThrow(new InvalidEventOperationException(
                        "На указанной локации уже запланировано событие на это время"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("На указанной локации уже запланировано событие на это время"));
    }

    @Test
    void updateUserEventById_InvalidStateAction_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.updateUserEventById(eq(userId), eq(eventId), any(UpdateEventUserRequest.class)))
                .thenThrow(new InvalidEventOperationException("Недопустимое действие для изменения состояния события"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Недопустимое действие для изменения состояния события"));
    }

    @Test
    void getUserEventRequestsByUserId_ReturnsRequests() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.getUserEventRequestsByUserId(userId, eventId))
                .thenReturn(List.of(participationRequestDto));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(participationRequestDto.getId()))
                .andExpect(jsonPath("$[0].event").value(participationRequestDto.getEvent()))
                .andExpect(jsonPath("$[0].requester").value(participationRequestDto.getRequester()))
                .andExpect(jsonPath("$[0].status").value(participationRequestDto.getStatus()));

        verify(eventService, times(1)).getUserEventRequestsByUserId(userId, eventId);
    }

    @Test
    void getUserEventRequestsByUserId_UserNotFound_ThrowsException() throws Exception {
        Long userId = 999L;
        Long eventId = 1L;

        when(eventService.getUserEventRequestsByUserId(userId, eventId))
                .thenThrow(new NotFoundException("Пользователь с id = 999 не найден"));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id = 999 не найден"));

        verify(eventService, times(1)).getUserEventRequestsByUserId(userId, eventId);
    }

    @Test
    void getUserEventRequestsByUserId_EventNotFound_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 999L;

        when(eventService.getUserEventRequestsByUserId(userId, eventId))
                .thenThrow(new NotFoundException("Событие с id = 999 не найдено"));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Событие с id = 999 не найдено"));

        verify(eventService, times(1)).getUserEventRequestsByUserId(userId, eventId);
    }

    @Test
    void getUserEventRequestsByUserId_UserNotInitiator_ThrowsException() throws Exception {
        Long userId = 10L;
        Long eventId = 1L;

        when(eventService.getUserEventRequestsByUserId(userId, eventId))
                .thenThrow(new ConditionsNotMetException(
                        "Пользователь с id = 10 не является инициатором события id = 1"));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Пользователь с id = 10 не является инициатором события id = 1"));

        verify(eventService, times(1)).getUserEventRequestsByUserId(userId, eventId);
    }

    @Test
    void getUserEventRequestsByUserId_NoRequests_ReturnsEmptyList() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.getUserEventRequestsByUserId(userId, eventId))
                .thenReturn(List.of());

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(eventService, times(1)).getUserEventRequestsByUserId(userId, eventId);
    }

    @Test
    void updateUserEventRequestsByUserId_ConfirmRequests_Success() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(participationRequestDto))
                .rejectedRequests(List.of())
                .build();

        when(eventService.updateUserEventRequestsByUserId(eq(userId), eq(eventId), any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(result);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequestStatusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(participationRequestDto.getId()))
                .andExpect(jsonPath("$.confirmedRequests[0].status").value("CONFIRMED"));

        verify(eventService, times(1)).updateUserEventRequestsByUserId(eq(userId), eq(eventId), any(EventRequestStatusUpdateRequest.class));
    }

    @Test
    void updateUserEventRequestsByUserId_UserNotFound_ThrowsException() throws Exception {
        Long userId = 999L;
        Long eventId = 1L;

        when(eventService.updateUserEventRequestsByUserId(eq(userId), eq(eventId), any(EventRequestStatusUpdateRequest.class)))
                .thenThrow(new NotFoundException("Пользователь с id = 999 не найден"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequestStatusUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id = 999 не найден"));
    }

    @Test
    void updateUserEventRequestsByUserId_EventNotFound_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 999L;

        when(eventService.updateUserEventRequestsByUserId(eq(userId), eq(eventId), any(EventRequestStatusUpdateRequest.class)))
                .thenThrow(new NotFoundException("Событие с id = 999 не найдено"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequestStatusUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Событие с id = 999 не найдено"));
    }

    @Test
    void updateUserEventRequestsByUserId_RequestNotPending_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.updateUserEventRequestsByUserId(eq(userId), eq(eventId), any(EventRequestStatusUpdateRequest.class)))
                .thenThrow(new InvalidEventOperationException("Статус можно изменить только у заявок, находящихся в состоянии ожидания"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequestStatusUpdateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Статус можно изменить только у заявок, находящихся в состоянии ожидания"));
    }

    @Test
    void updateUserEventRequestsByUserId_LimitReached_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(eventService.updateUserEventRequestsByUserId(eq(userId), eq(eventId), any(EventRequestStatusUpdateRequest.class)))
                .thenThrow(new InvalidEventOperationException("Нельзя подтвердить заявку, лимит участников уже достигнут"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequestStatusUpdateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Нельзя подтвердить заявку, лимит участников уже достигнут"));
    }

}
