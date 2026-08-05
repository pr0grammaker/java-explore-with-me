package integration.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.PrivateAppExploreWithMe;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.event.*;
import ru.practicum.exceptions.ConditionsNotMetException;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.participationrequest.ParticipationRequest;
import ru.practicum.participationrequest.ParticipationRequestDto;
import ru.practicum.participationrequest.ParticipationRequestRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PrivateAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class PrivateAppExploreWithMeEventServiceTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventMapper eventMapper;

    private Category category;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParticipationRequestRepository participationRequestRepository;


    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("Концерт")
                .build());
    }

    private EventFullDto createEventFullDto(User initiator, EventState state, LocalDateTime eventDate) {
        Event event = Event.builder()
                .annotation("Аннотация")
                .category(category)
                .confirmedRequests(5L)
                .createdOn(LocalDateTime.now())
                .description("Описание")
                .eventDate(eventDate)
                .initiator(initiator)
                .location(new Location(51.1694f, 71.4491f))
                .paid(true)
                .participantLimit(10L)
                .publishedOn(LocalDateTime.now())
                .requestModeration(true)
                .state(state)
                .title("Новое событие")
                .views(100L)
                .build();

        eventRepository.save(event);
        return eventMapper.mapToEventFullDto(event, 5L, 100L);
    }

    private EventShortDto buildEventShortDto(Long categoryId, LocalDateTime eventDate, String title) {
        return EventShortDto.builder()
                .annotation("Аннотация")
                .description("Описание")
                .title(title)
                .category(categoryId)
                .eventDate(eventDate)
                .location(new Location(51.1694f, 71.4491f))
                .paid(true)
                .participantLimit(10L)
                .requestModeration(true)
                .build();
    }

    private UpdateEventUserRequest buildUpdateEventUserRequest(Long categoryId, LocalDateTime eventDate, String title) {
        return UpdateEventUserRequest.builder()
                .annotation("Аннотация для события, достаточно длинная")
                .description("Описание события, которое содержит больше двадцати символов")
                .title(title)
                .category(categoryId)
                .eventDate(eventDate)
                .location(new Location(51.1694f, 71.4491f))
                .paid(true)
                .participantLimit(10L)
                .requestModeration(true)
                .stateAction(StateAction.SEND_TO_REVIEW)
                .build();
    }

    private ParticipationRequest createParticipationRequest(User requester, Event event, RequestStatus status) {
        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();
        return participationRequestRepository.save(request);
    }

    private EventRequestStatusUpdateRequest buildStatusUpdateRequest(List<Long> requestIds, RequestStatus status) {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(requestIds);
        updateRequest.setStatus(status);
        return updateRequest;
    }

    @Test
    void getEventsByUser_ReturnsEvents() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        createEventFullDto(user, EventState.PUBLISHED, LocalDateTime.now().plusDays(1));
        createEventFullDto(user, EventState.PUBLISHED, LocalDateTime.now().plusDays(2));

        Collection<EventFullDto> events = eventService.getEventsByUser(user.getId(), 0, 10);

        assertThat(events).hasSize(2);
    }


    @Test
    void getEventsByUser_PaginationWorks() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventFullDto e1 = createEventFullDto(user, EventState.PUBLISHED, LocalDateTime.now().plusDays(1));
        createEventFullDto(user, EventState.PUBLISHED, LocalDateTime.now().plusDays(2));
        createEventFullDto(user, EventState.PUBLISHED, LocalDateTime.now().plusDays(3));

        Collection<EventFullDto> events = eventService.getEventsByUser(e1.getInitiator().getId(), 0, 2);

        assertThat(events).hasSize(2);
    }

    @Test
    void getEventsByUser_NoEvents_ReturnsEmptyList() {
        User user = userRepository.save(User.builder()
                .name("Пустой пользователь")
                .email("empty@example.com")
                .build());

        Collection<EventFullDto> events = eventService.getEventsByUser(user.getId(), 0, 10);

        assertThat(events).isEmpty();
    }

    @Test
    void addEvent_Success() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventShortDto dto = buildEventShortDto(category.getId(), LocalDateTime.now().plusHours(3), "Новое событие");

        EventFullDto created = eventService.addEvent(user.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Новое событие");
        assertThat(created.getState()).isEqualTo(EventState.PENDING);
    }

    @Test
    void addEvent_UserNotFound_ThrowsException() {
        EventShortDto dto = buildEventShortDto(category.getId(), LocalDateTime.now().plusHours(3), "Новое событие");

        assertThatThrownBy(() -> eventService.addEvent(999L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id = 999 не найден");
    }

    @Test
    void addEvent_CategoryNotFound_ThrowsException() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventShortDto dto = buildEventShortDto(999L, LocalDateTime.now().plusHours(3), "Новое событие");

        assertThatThrownBy(() -> eventService.addEvent(user.getId(), dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Категория с id = 999 не найдена");
    }

    @Test
    void addEvent_EventDateTooSoon_ThrowsException() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventShortDto dto = buildEventShortDto(category.getId(), LocalDateTime.now().plusMinutes(30), "Новое событие");

        assertThatThrownBy(() -> eventService.addEvent(user.getId(), dto))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessageContaining("Дата и время на которые намечено событие не может быть раньше");
    }

    @Test
    void addEvent_DuplicateLocationAndDate_ThrowsException() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        // здесь нормально работает если fixedDate указывать конкретное время
        LocalDateTime fixedDate = LocalDateTime.of(2026, 8, 5, 23, 0, 0);
        Location fixedLocation = new Location(51.1694f, 71.4491f);

        EventShortDto dto1 = buildEventShortDto(category.getId(), fixedDate, "Событие 1");
        dto1.setLocation(fixedLocation);
        eventService.addEvent(user.getId(), dto1);

        EventShortDto dto2 = buildEventShortDto(category.getId(), fixedDate, "Событие 2");
        dto2.setLocation(fixedLocation);

        assertThatThrownBy(() -> eventService.addEvent(user.getId(), dto2))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("На указанной локации уже запланировано событие на это время");
    }


    @Test
    void getUserEventById_Success() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventFullDto dto = createEventFullDto(user, EventState.PUBLISHED, LocalDateTime.now().plusDays(1));

        EventFullDto found = eventService.getUserEventById(user.getId(), dto.getId());

        assertThat(found.getId()).isEqualTo(dto.getId());
        assertThat(found.getInitiator().getId()).isEqualTo(user.getId());
        assertThat(found.getTitle()).isEqualTo("Новое событие");
    }

    @Test
    void getUserEventById_UserNotFound_ThrowsException() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventFullDto dto = createEventFullDto(user, EventState.PUBLISHED, LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> eventService.getUserEventById(999L, dto.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id = 999 не найден");
    }

    @Test
    void getUserEventById_EventNotFound_ThrowsException() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        assertThatThrownBy(() -> eventService.getUserEventById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Событие с id = 999 не найдено");
    }

    @Test
    void getUserEventById_UserNotInitiator_ThrowsException() {
        User initiator = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        User otherUser = userRepository.save(User.builder()
                .name("Пётр Петров")
                .email("petr@example.com")
                .build());

        EventFullDto dto = createEventFullDto(initiator, EventState.PUBLISHED, LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> eventService.getUserEventById(otherUser.getId(), dto.getId()))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessage("Пользователь с id = %d не является инициатором события id = %d"
                        .formatted(otherUser.getId(), dto.getId()));
    }

    @Test
    void updateUserEventById_Success() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(user, EventState.PENDING, LocalDateTime.now().plusDays(3));

        UpdateEventUserRequest updateRequest = buildUpdateEventUserRequest(
                category.getId(),
                LocalDateTime.now().plusDays(5),
                "Обновлённый заголовок"
        );

        EventFullDto updated = eventService.updateUserEventById(user.getId(), eventDto.getId(), updateRequest);

        assertThat(updated.getTitle()).isEqualTo("Обновлённый заголовок");
        assertThat(updated.getState()).isEqualTo(EventState.PENDING);
    }

    @Test
    void updateUserEventById_UserNotFound_ThrowsException() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(user, EventState.PENDING, LocalDateTime.now().plusDays(3));

        UpdateEventUserRequest updateRequest = buildUpdateEventUserRequest(
                category.getId(),
                LocalDateTime.now().plusDays(5),
                "Заголовок"
        );

        assertThatThrownBy(() -> eventService.updateUserEventById(999L, eventDto.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id = 999 не найден");
    }

    @Test
    void updateUserEventById_EventNotFound_ThrowsException() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        UpdateEventUserRequest updateRequest = buildUpdateEventUserRequest(
                category.getId(),
                LocalDateTime.now().plusDays(5),
                "Заголовок"
        );

        assertThatThrownBy(() -> eventService.updateUserEventById(user.getId(), 999L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Событие с id = 999 не найдено");
    }

    @Test
    void updateUserEventById_UserNotInitiator_ThrowsException() {
        User initiator = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        User otherUser = userRepository.save(User.builder()
                .name("Пётр Петров")
                .email("petr@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(initiator, EventState.PENDING, LocalDateTime.now().plusDays(3));

        UpdateEventUserRequest updateRequest = buildUpdateEventUserRequest(
                category.getId(),
                LocalDateTime.now().plusDays(5),
                "Заголовок"
        );

        assertThatThrownBy(() -> eventService.updateUserEventById(otherUser.getId(), eventDto.getId(), updateRequest))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Пользователь не является инициатором события");
    }

    @Test
    void updateUserEventById_EventDateTooSoon_ThrowsException() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(user, EventState.PENDING, LocalDateTime.now().plusDays(3));

        UpdateEventUserRequest updateRequest = buildUpdateEventUserRequest(
                category.getId(),
                LocalDateTime.now().plusMinutes(30),
                "Заголовок"
        );

        assertThatThrownBy(() -> eventService.updateUserEventById(user.getId(), eventDto.getId(), updateRequest))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessageContaining("Дата и время на которые намечено событие не может быть раньше");
    }

    @Test
    void updateUserEventById_InvalidState_ThrowsException() {
        User user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(user, EventState.PUBLISHED, LocalDateTime.now().plusDays(3));

        UpdateEventUserRequest updateRequest = buildUpdateEventUserRequest(
                category.getId(),
                LocalDateTime.now().plusDays(5),
                "Заголовок"
        );

        assertThatThrownBy(() -> eventService.updateUserEventById(user.getId(), eventDto.getId(), updateRequest))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Изменить можно только отмененные события или события в состоянии ожидания модерации");
    }

    @Test
    void getUserEventRequestsByUserId_ReturnsRequests() {
        User initiator = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        User requester1 = userRepository.save(User.builder()
                .name("Пётр Петров")
                .email("petr@example.com")
                .build());

        User requester2 = userRepository.save(User.builder()
                .name("Сергей Сергеев")
                .email("sergey@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(initiator, EventState.PUBLISHED, LocalDateTime.now().plusDays(3));
        Event event = eventRepository.findById(eventDto.getId()).get();

        createParticipationRequest(requester1, event, RequestStatus.CONFIRMED);
        createParticipationRequest(requester2, event, RequestStatus.PENDING);

        Collection<ParticipationRequestDto> requests =
                eventService.getUserEventRequestsByUserId(initiator.getId(), event.getId());

        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(ParticipationRequestDto::getStatus)
                .containsExactlyInAnyOrder("CONFIRMED", "PENDING");
    }

    @Test
    void getUserEventRequestsByUserId_UserNotFound_ThrowsException() {
        User initiator = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(initiator, EventState.PUBLISHED, LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> eventService.getUserEventRequestsByUserId(999L, eventDto.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id = 999 не найден");
    }

    @Test
    void getUserEventRequestsByUserId_EventNotFound_ThrowsException() {
        User initiator = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        assertThatThrownBy(() -> eventService.getUserEventRequestsByUserId(initiator.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Событие с id = 999 не найдено");
    }

    @Test
    void getUserEventRequestsByUserId_UserNotInitiator_ThrowsException() {
        User initiator = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        User otherUser = userRepository.save(User.builder()
                .name("Пётр Петров")
                .email("petr@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(initiator, EventState.PUBLISHED, LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> eventService.getUserEventRequestsByUserId(otherUser.getId(), eventDto.getId()))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessage("Пользователь с id = %d не является инициатором события id = %d"
                        .formatted(otherUser.getId(), eventDto.getId()));
    }

    @Test
    void getUserEventRequestsByUserId_NoRequests_ReturnsEmptyList() {
        User initiator = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(initiator, EventState.PUBLISHED, LocalDateTime.now().plusDays(3));

        Collection<ParticipationRequestDto> requests =
                eventService.getUserEventRequestsByUserId(initiator.getId(), eventDto.getId());

        assertThat(requests).isEmpty();
    }


    @Test
    void updateUserEventRequestsByUserId_ConfirmRequests_Success() {
        User initiator = userRepository.save(User.builder()
                .name("Инициатор")
                .email("init@example.com")
                .build());

        User requester = userRepository.save(User.builder()
                .name("Участник")
                .email("req@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(initiator, EventState.PENDING, LocalDateTime.now().plusDays(3));
        Event event = eventRepository.findById(eventDto.getId()).get();

        ParticipationRequest request = createParticipationRequest(requester, event, RequestStatus.PENDING);

        EventRequestStatusUpdateRequest updateRequest =
                buildStatusUpdateRequest(List.of(request.getId()), RequestStatus.CONFIRMED);

        EventRequestStatusUpdateResult result =
                eventService.updateUserEventRequestsByUserId(initiator.getId(), event.getId(), updateRequest);

        assertThat(result.getConfirmedRequests()).hasSize(1);
        assertThat(result.getConfirmedRequests().getFirst().getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void updateUserEventRequestsByUserId_RejectRequests_Success() {
        User initiator = userRepository.save(User.builder()
                .name("Инициатор")
                .email("init@example.com")
                .build());

        User requester = userRepository.save(User.builder()
                .name("Участник")
                .email("req@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(initiator, EventState.PENDING, LocalDateTime.now().plusDays(3));
        Event event = eventRepository.findById(eventDto.getId()).get();

        ParticipationRequest request = createParticipationRequest(requester, event, RequestStatus.PENDING);

        EventRequestStatusUpdateRequest updateRequest =
                buildStatusUpdateRequest(List.of(request.getId()), RequestStatus.REJECTED);

        EventRequestStatusUpdateResult result =
                eventService.updateUserEventRequestsByUserId(initiator.getId(), event.getId(), updateRequest);

        assertThat(result.getRejectedRequests()).hasSize(1);
        assertThat(result.getRejectedRequests().getFirst().getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void updateUserEventRequestsByUserId_RequestNotPending_ThrowsException() {
        User initiator = userRepository.save(User.builder()
                .name("Инициатор")
                .email("init@example.com")
                .build());

        User requester = userRepository.save(User.builder()
                .name("Участник")
                .email("req@example.com")
                .build());

        EventFullDto eventDto = createEventFullDto(initiator, EventState.PENDING, LocalDateTime.now().plusDays(3));
        Event event = eventRepository.findById(eventDto.getId()).get();

        ParticipationRequest request = createParticipationRequest(requester, event, RequestStatus.CONFIRMED);

        EventRequestStatusUpdateRequest updateRequest =
                buildStatusUpdateRequest(List.of(request.getId()), RequestStatus.REJECTED);

        assertThatThrownBy(() -> eventService.updateUserEventRequestsByUserId(initiator.getId(), event.getId(), updateRequest))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Статус можно изменить только у заявок, находящихся в состоянии ожидания");
    }

    @Test
    void updateUserEventRequestsByUserId_LimitReached_ThrowsException() {
        User initiator = userRepository.save(User.builder()
                .name("Инициатор")
                .email("init@example.com")
                .build());

        User requester = userRepository.save(User.builder()
                .name("Участник")
                .email("req@example.com")
                .build());

        Event event = Event.builder()
                .annotation("Аннотация")
                .category(category)
                .confirmedRequests(0L)
                .createdOn(LocalDateTime.now())
                .description("Описание")
                .eventDate(LocalDateTime.now().plusDays(3))
                .initiator(initiator)
                .location(new Location(51.1694f, 71.4491f))
                .paid(true)
                .participantLimit(1L)
                .requestModeration(true)
                .state(EventState.PENDING)
                .title("Событие")
                .views(0L)
                .build();
        eventRepository.save(event);

        createParticipationRequest(requester, event, RequestStatus.CONFIRMED);

        User requester2 = userRepository.save(User.builder()
                .name("Участник2")
                .email("req2@example.com")
                .build());
        ParticipationRequest pending = createParticipationRequest(requester2, event, RequestStatus.PENDING);

        EventRequestStatusUpdateRequest updateRequest =
                buildStatusUpdateRequest(List.of(pending.getId()), RequestStatus.CONFIRMED);

        assertThatThrownBy(() -> eventService.updateUserEventRequestsByUserId(initiator.getId(), event.getId(), updateRequest))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Нельзя подтвердить заявку, лимит участников уже достигнут");
    }



}

