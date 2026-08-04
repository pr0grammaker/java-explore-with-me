package integration.compilation;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.compilation.*;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.event.Location;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = AdminAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class AdminAppExploreWithMeCompilationServiceTest {

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private CompilationAdminService compilationAdminService;

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

    private CompilationDto createCompilationDto(List<Long> events, boolean pinned, String title) {
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .events(events)
                .pinned(pinned)
                .title(title)
                .build();

        return compilationAdminService.addCompilation(newCompilationDto);
    }

    @Test
    void addCompilation_WithEvents_Success() {
        Event e1 = createEvent("Событие 1");
        Event e2 = createEvent("Событие 2");

        CompilationDto dto = createCompilationDto(List.of(e1.getId(), e2.getId()), true, "Летние концерты");

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getTitle()).isEqualTo("Летние концерты");
        assertThat(dto.getPinned()).isTrue();
        assertThat(dto.getEvents()).hasSize(2);
    }

    @Test
    void addCompilation_WithoutEvents_Success() {
        CompilationDto dto = createCompilationDto(Collections.emptyList(), false, "Пустая подборка");

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getTitle()).isEqualTo("Пустая подборка");
        assertThat(dto.getPinned()).isFalse();
        assertThat(dto.getEvents()).isEmpty();
    }

    @Test
    void addCompilation_NullEvents_Success() {
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .events(Collections.emptyList())
                .pinned(true)
                .title("Без событий")
                .build();

        CompilationDto dto = compilationAdminService.addCompilation(newCompilationDto);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getTitle()).isEqualTo("Без событий");
        assertThat(dto.getEvents()).isEmpty();
    }

    @Test
    void addCompilation_DefaultPinnedValue() {
        Event e1 = createEvent("Событие 1");

        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .events(List.of(e1.getId()))
                .title("Подборка без pinned")
                .build();

        CompilationDto dto = compilationAdminService.addCompilation(newCompilationDto);

        assertThat(dto.getPinned()).isFalse();
    }

    @Test
    void deleteCompilation_Success() {
        Compilation compilation = Compilation.builder()
                .title("Тестовая подборка")
                .pinned(true)
                .build();
        compilation = compilationRepository.save(compilation);

        compilationAdminService.deleteCompilation(compilation.getId());

        assertThat(compilationRepository.findById(compilation.getId())).isEmpty();
    }

    @Test
    void deleteCompilation_NotFound_ThrowsException() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> compilationAdminService.deleteCompilation(nonExistentId))
                .isInstanceOf(ru.practicum.exceptions.NotFoundException.class)
                .hasMessageContaining("Подборка с id=999 не найдена");
    }

    @Test
    void updateCompilation_UpdateTitleAndPinned() {
        CompilationDto dto = createCompilationDto(Collections.emptyList(), false, "Старая подборка");

        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("Новая подборка")
                .pinned(true)
                .build();

        CompilationDto updated = compilationAdminService.updateCompilation(dto.getId(), request);

        assertThat(updated.getTitle()).isEqualTo("Новая подборка");
        assertThat(updated.getPinned()).isTrue();
    }

    @Test
    void updateCompilation_UpdateEvents() {
        Event e1 = createEvent("Событие 1");
        Event e2 = createEvent("Событие 2");

        CompilationDto dto = createCompilationDto(Collections.emptyList(), false, "Подборка");

        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .events(List.of(e1.getId(), e2.getId()))
                .build();

        CompilationDto updated = compilationAdminService.updateCompilation(dto.getId(), request);

        assertThat(updated.getEvents()).hasSize(2);
        assertThat(updated.getEvents().getFirst().getTitle()).isIn("Событие 1", "Событие 2");
    }

    @Test
    void updateCompilation_PartialUpdate() {
        CompilationDto dto = createCompilationDto(Collections.emptyList(), false, "Подборка");

        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .pinned(true)
                .build();

        CompilationDto updated = compilationAdminService.updateCompilation(dto.getId(), request);

        assertThat(updated.getPinned()).isTrue();
        assertThat(updated.getTitle()).isEqualTo("Подборка");
    }

    @Test
    void updateCompilation_NotFound_ThrowsException() {
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("Новая подборка")
                .build();

        assertThatThrownBy(() -> compilationAdminService.updateCompilation(999L, request))
                .isInstanceOf(ru.practicum.exceptions.NotFoundException.class)
                .hasMessageContaining("Подборка с id=999 не найдена");
    }



}
