package integration.compilation;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.PublicAppExploreWithMe;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.compilation.*;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.event.Location;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PublicAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class PublicAppExploreWithMeCompilationServiceTest {

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private CompilationMapper compilationMapper;

    @Autowired
    private CompilationPublicService compilationPublicService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Category category;
    private User initiator;

    @BeforeEach
    void setup() {
        compilationRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("Концерт")
                .build());

        initiator = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan" + UUID.randomUUID() + "@example.com")
                .build());
    }

    private Event createEvent(String title) {
        Event event = Event.builder()
                .annotation("Аннотация")
                .category(category)
                .createdOn(LocalDateTime.now())
                .description("Описание")
                .eventDate(LocalDateTime.now().plusDays(1))
                .initiator(initiator)
                .location(new Location(51.1694f, 71.4491f))
                .paid(true)
                .participantLimit(10L)
                .publishedOn(LocalDateTime.now())
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .title(title)
                .views(100L)
                .build();
        return eventRepository.save(event);
    }

    private CompilationDto createCompilationDto(Set<Event> events, boolean pinned, String title) {
        Compilation compilation = Compilation.builder()
                .events(events)
                .pinned(pinned)
                .title(title)
                .build();

        compilationRepository.save(compilation);

        return compilationMapper.mapToCompilationDto(compilation);
    }

    @Test
    void getAllCompilations_ReturnsPinnedCompilations() {
        Event e1 = createEvent("Событие 1");
        Event e2 = createEvent("Событие 2");

        createCompilationDto(Set.of(e1, e2), true, "Летние концерты");
        createCompilationDto(Set.of(e1), false, "Осенние концерты");

        Collection<CompilationDto> compilations = compilationPublicService.getAllCompilations(true, 0, 10);

        assertThat(compilations).hasSize(1);
        assertThat(compilations.iterator().next().getTitle()).isEqualTo("Летние концерты");
    }

    @Test
    void getAllCompilations_PaginationWorks() {
        Event e1 = createEvent("Событие 1");
        Event e2 = createEvent("Событие 2");
        Event e3 = createEvent("Событие 3");

        createCompilationDto(Set.of(e1), true, "Подборка 1");
        createCompilationDto(Set.of(e2), true, "Подборка 2");
        createCompilationDto(Set.of(e3), true, "Подборка 3");

        Collection<CompilationDto> compilations = compilationPublicService.getAllCompilations(true, 0, 2);

        assertThat(compilations).hasSize(2);
    }

    @Test
    void getCompilationById_ReturnsCompilation() {
        Event e1 = createEvent("Событие 1");
        CompilationDto dto = createCompilationDto(Set.of(e1), true, "Подборка");

        CompilationDto found = compilationPublicService.getCompilationById(dto.getId());

        assertThat(found.getId()).isEqualTo(dto.getId());
        assertThat(found.getTitle()).isEqualTo("Подборка");
        assertThat(found.getEvents()).hasSize(1);
    }

    @Test
    void getCompilationById_NotFound_ThrowsException() {
        assertThatThrownBy(() -> compilationPublicService.getCompilationById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Подборка с id=999 не найдена");
    }


}
