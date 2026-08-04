package integration.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.event.*;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = AdminAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class AdminAppExploreWithMeEventAdminServiceTest {


    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventAdminService eventAdminService;

    @Autowired
    private EventMapper eventMapper;

    private Category category;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("Концерт")
                .build());
    }

    private EventFullDto createEventFullDto(EventState state, LocalDateTime eventDate) {
        String email = "user" + UUID.randomUUID() + "@example.com";
        User initiator = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email(email)
                .build());

        Event event = Event.builder()
                .annotation("Аннотация")
                .category(category)
                .confirmedRequests(5L)
                .createdOn(LocalDateTime.now())
                .description("Описание")
                .eventDate(eventDate)
                .initiator(initiator) // теперь инициатор реально существует в БД
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


    @Test
    void updateEvent_PublishSuccess() {
        EventFullDto dto = createEventFullDto(EventState.PENDING, LocalDateTime.now().plusHours(3));

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();

        EventFullDto updated = eventAdminService.updateEvent(dto.getId(), request);

        assertThat(updated.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(updated.getPublishedOn()).isNotNull();
    }

    @Test
    void updateEvent_RejectPublishedEvent_ThrowsException() {
        EventFullDto dto = createEventFullDto(EventState.PUBLISHED, LocalDateTime.now().plusHours(3));

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.REJECT_EVENT)
                .build();

        assertThatThrownBy(() -> eventAdminService.updateEvent(dto.getId(), request))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessageContaining("Событие нельзя отклонить");
    }

    @Test
    void updateEvent_CategoryNotFound_ThrowsException() {
        EventFullDto dto = createEventFullDto(EventState.PENDING, LocalDateTime.now().plusHours(3));

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .category(999L)
                .build();

        assertThatThrownBy(() -> eventAdminService.updateEvent(dto.getId(), request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Категория с id = 999 не найдена");
    }

    @Test
    void updateEvent_LocationConflict_ThrowsException() {
        EventFullDto dto1 = createEventFullDto(EventState.PENDING, LocalDateTime.now().plusHours(3));
        createEventFullDto(EventState.PENDING, dto1.getEventDate());

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .title("Обновленное событие")
                .build();

        assertThatThrownBy(() -> eventAdminService.updateEvent(dto1.getId(), request))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessageContaining("На указанной локации уже запланировано событие");
    }

    @Test
    void getEvents_ReturnsEventsForUserAndCategory() {
        EventFullDto dto = createEventFullDto(EventState.PUBLISHED, LocalDateTime.now().plusDays(1));

        var result = eventAdminService.getEvents(
                List.of(dto.getInitiator().getId().intValue()),
                List.of("PUBLISHED"),
                List.of(dto.getCategory().getId().intValue()),
                "2026-08-01 00:00:00",
                "2026-08-10 23:59:59",
                0,
                10
        );

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getId()).isEqualTo(dto.getId());
    }

    @Test
    void getEvents_EmptyWhenNoMatch() {
        createEventFullDto(EventState.PUBLISHED, LocalDateTime.now().plusDays(1));

        var result = eventAdminService.getEvents(
                List.of(999),
                List.of("PUBLISHED"),
                List.of(category.getId().intValue()),
                "2026-08-01 00:00:00",
                "2026-08-10 23:59:59",
                0,
                10
        );

        assertThat(result).isEmpty();
    }

    @Test
    void getEvents_FilterByDateRange() {
        createEventFullDto(EventState.PUBLISHED, LocalDateTime.of(2026, 8, 5, 12, 0));
        createEventFullDto(EventState.PUBLISHED, LocalDateTime.of(2026, 8, 20, 12, 0));

        var result = eventAdminService.getEvents(
                List.of(category.getId().intValue()), // инициатор id
                List.of("PUBLISHED"),
                List.of(category.getId().intValue()),
                "2026-08-01 00:00:00",
                "2026-08-10 23:59:59",
                0,
                10
        );

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getEventDate()).isEqualTo(LocalDateTime.of(2026, 8, 5, 12, 0));
    }

    @Test
    void getEvents_PaginationWorks() {
        for (int i = 0; i < 15; i++) {
            createEventFullDto(EventState.PUBLISHED, LocalDateTime.now().plusDays(i + 1));
        }

        var page1 = eventAdminService.getEvents(
                userRepository.findAll().stream().map(u -> u.getId().intValue()).toList(),
                List.of("PUBLISHED"),
                List.of(category.getId().intValue()),
                "2026-08-01 00:00:00",
                "2026-08-30 23:59:59",
                0,
                10
        );

        var page2 = eventAdminService.getEvents(
                userRepository.findAll().stream().map(u -> u.getId().intValue()).toList(),
                List.of("PUBLISHED"),
                List.of(category.getId().intValue()),
                "2026-08-01 00:00:00",
                "2026-08-30 23:59:59",
                10,
                10
        );

        assertThat(page1).hasSize(10);
        assertThat(page2).hasSize(5);
    }





}

