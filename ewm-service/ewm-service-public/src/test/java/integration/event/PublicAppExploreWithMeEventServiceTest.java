package integration.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.PublicAppExploreWithMe;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.event.*;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.http.client.EndpointHttpClient;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PublicAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class PublicAppExploreWithMeEventServiceTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventPublicService eventPublicService;

    @Autowired
    private EventMapper eventMapper;

    private Category category;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private EndpointHttpClient endpointHttpClient; // используется для тестов


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

    @Test
    void getAllEvents_ReturnsPublishedEvents() {
        createEventFullDto(EventState.PUBLISHED, LocalDateTime.now().plusDays(1));
        createEventFullDto(EventState.PUBLISHED, LocalDateTime.now().plusDays(2));

        Collection<EventFullDto> events = eventPublicService.getAllEvents(
                "", List.of(category.getId()), true,
                null, null, false, "EVENT_DATE", 0, 10,
                new MockHttpServletRequest());

        assertThat(events).hasSize(2);
        assertThat(events).extracting(EventFullDto::getTitle).contains("Новое событие");
    }

    @Test
    void getAllEvents_FilterByDateRange() {
        createEventFullDto(EventState.PUBLISHED, LocalDateTime.now().plusDays(1));
        createEventFullDto(EventState.PUBLISHED, LocalDateTime.now().plusDays(10));

        String start = LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = LocalDateTime.now().plusDays(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Collection<EventFullDto> events = eventPublicService.getAllEvents(
                "", List.of(category.getId()), true,
                start, end, false, "EVENT_DATE", 0, 10,
                new MockHttpServletRequest());

        assertThat(events).hasSize(1);
    }

    @Test
    void getEventById_ReturnsEvent() {
        EventFullDto dto = createEventFullDto(EventState.PUBLISHED, LocalDateTime.now().plusDays(1));

        EventFullDto found = eventPublicService.getEventById(dto.getId(), new MockHttpServletRequest());

        assertThat(found.getId()).isEqualTo(dto.getId());
        assertThat(found.getTitle()).isEqualTo("Новое событие");
    }

    @Test
    void getEventById_NotFound_ThrowsException() {
        assertThatThrownBy(() -> eventPublicService.getEventById(999L, new MockHttpServletRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Событие по id=999 не найдено");
    }

    @Test
    void getEventById_NotPublished_ThrowsException() {
        EventFullDto dto = createEventFullDto(EventState.PENDING, LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> eventPublicService.getEventById(dto.getId(), new MockHttpServletRequest()))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Событие должно быть опубликовано");
    }

}

