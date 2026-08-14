package integration.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.comment.Comment;
import ru.practicum.comment.CommentAdminService;
import ru.practicum.comment.CommentDto;

import ru.practicum.comment.CommentRepository;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.event.Location;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = AdminAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
@Transactional
public class AdminAppExploreWithMeCommentServiceTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentAdminService commentAdminService;

    @Autowired
    private UserRepository userRepository;

    private Category category;


    @BeforeEach
    void setup() {
        commentRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("Концерт")
                .build());
    }

    private User createUser(String name) {
        String email = "user" + UUID.randomUUID() + "@example.com";
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .build());
    }

    private Event createEvent(EventState state, boolean allowComments) {
        User initiator = createUser("Anton");

        Event event = Event.builder()
                .annotation("Аннотация события")
                .category(category)
                .confirmedRequests(5L)
                .createdOn(LocalDateTime.now())
                .description("Описание события")
                .eventDate(LocalDateTime.now().plusDays(1))
                .initiator(initiator)
                .location(new Location(51.1694f, 71.4491f))
                .paid(true)
                .participantLimit(10L)
                .publishedOn(LocalDateTime.now())
                .requestModeration(true)
                .state(state)
                .allowComments(allowComments)
                .commentsCount(0L)
                .title("Новое событие")
                .views(100L)
                .build();

        return eventRepository.save(event);
    }

    private Comment createComment(User author, Event event, String text, LocalDateTime createdOn) {
        Comment comment = Comment.builder()
                .text(text)
                .event(event)
                .author(author)
                .createdOn(createdOn)
                .build();

        return commentRepository.save(comment);
    }

    @Test
    void searchComments_WithoutFilters_ReturnsAll() {
        User user1 = createUser("Ivan");
        User user2 = createUser("Petr");
        Event event = createEvent(EventState.PUBLISHED, true);

        LocalDateTime now = LocalDateTime.now();
        Comment comment1 = createComment(user1, event, "Первый комментарий", now.minusHours(2));
        Comment comment2 = createComment(user2, event, "Второй комментарий", now.minusHours(1));

        Collection<CommentDto> result = commentAdminService.searchComments(null, null, null, 0, 10);

        assertThat(result).hasSize(2);
        List<CommentDto> list = result.stream().toList();
        assertThat(list.get(0).getId()).isEqualTo(comment2.getId());
        assertThat(list.get(1).getId()).isEqualTo(comment1.getId());
    }

    @Test
    void searchComments_FilterByUsers() {
        User user1 = createUser("Ivan");
        User user2 = createUser("Petr");
        Event event = createEvent(EventState.PUBLISHED, true);

        createComment(user1, event, "Комментарий от Ivan", LocalDateTime.now());
        Comment comment2 = createComment(user2, event, "Комментарий от Petr", LocalDateTime.now());

        Collection<CommentDto> result = commentAdminService.searchComments(List.of(user2.getId()), null, null, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getId()).isEqualTo(comment2.getId());
    }

    @Test
    void searchComments_FilterByEvents() {
        User user = createUser("Ivan");
        Event event1 = createEvent(EventState.PUBLISHED, true);
        Event event2 = createEvent(EventState.PUBLISHED, true);

        createComment(user, event1, "Текст для события 1", LocalDateTime.now());
        Comment comment2 = createComment(user, event2, "Текст для события 2", LocalDateTime.now());

        Collection<CommentDto> result = commentAdminService.searchComments(null, List.of(event2.getId()), null, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getId()).isEqualTo(comment2.getId());
    }

    @Test
    void searchComments_FilterByText() {
        User user = createUser("Ivan");
        Event event = createEvent(EventState.PUBLISHED, true);

        createComment(user, event, "Обычный отзыв", LocalDateTime.now());
        Comment comment2 = createComment(user, event, "Важное замечание про звук", LocalDateTime.now());

        Collection<CommentDto> result = commentAdminService.searchComments(null, null, "звук", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getId()).isEqualTo(comment2.getId());
    }

    @Test
    void searchComments_CombinedFiltersAndPagination() {
        User user = createUser("Ivan");
        Event event = createEvent(EventState.PUBLISHED, true);

        LocalDateTime now = LocalDateTime.now();
        createComment(user, event, "Супер концерт 1", now.minusHours(3));
        Comment comment2 = createComment(user, event, "Супер концерт 2", now.minusHours(2));
        createComment(user, event, "Супер концерт 3", now.minusHours(1));

        Collection<CommentDto> result = commentAdminService.searchComments(
                List.of(user.getId()),
                List.of(event.getId()),
                "Супер",
                1, 1
        );

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getId()).isEqualTo(comment2.getId());
    }

    @Test
    void deleteCommentByAdmin_Success() {
        User user = createUser("Ivan");
        Event event = createEvent(EventState.PUBLISHED, true);
        event.setCommentsCount(1L);
        eventRepository.save(event);

        Comment comment = createComment(user, event, "Комментарий для удаления", LocalDateTime.now());

        commentAdminService.deleteCommentByAdmin(comment.getId());

        assertThat(commentRepository.findById(comment.getId())).isEmpty();

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getCommentsCount()).isEqualTo(0L);
    }

    @Test
    void deleteCommentByAdmin_NotFound_ThrowsNotFoundException() {
        assertThatThrownBy(() -> commentAdminService.deleteCommentByAdmin(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Комментарий пользователя с id=999 не найден");
    }
}