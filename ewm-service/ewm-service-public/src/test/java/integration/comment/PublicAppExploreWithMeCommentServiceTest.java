package integration.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.PublicAppExploreWithMe;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.comment.Comment;
import ru.practicum.comment.CommentDto;
import ru.practicum.comment.CommentPublicService;
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

@SpringBootTest(classes = PublicAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
@Transactional
public class PublicAppExploreWithMeCommentServiceTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentPublicService commentPublicService;

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
    void getEventComments_Success_DefaultSortDesc() {
        Event event = createEvent(EventState.PUBLISHED, true);
        User author = createUser("Ivan");

        LocalDateTime now = LocalDateTime.now();
        Comment comment1 = createComment(author, event, "Первый комментарий", now.minusHours(2));
        Comment comment2 = createComment(author, event, "Второй комментарий", now.minusHours(1));

        Collection<CommentDto> comments = commentPublicService.getEventComments(event.getId(), 0, 10, "DESC");

        assertThat(comments).hasSize(2);

        List<CommentDto> commentList = comments.stream().toList();

        assertThat(commentList.get(0).getId()).isEqualTo(comment2.getId());
        assertThat(commentList.get(0).getText()).isEqualTo("Второй комментарий");
        assertThat(commentList.get(1).getId()).isEqualTo(comment1.getId());
        assertThat(commentList.get(1).getText()).isEqualTo("Первый комментарий");
    }

    @Test
    void getEventComments_Success_SortAsc() {
        Event event = createEvent(EventState.PUBLISHED, true);
        User author = createUser("Ivan");

        LocalDateTime now = LocalDateTime.now();
        Comment comment1 = createComment(author, event, "Первый комментарий", now.minusHours(2));
        Comment comment2 = createComment(author, event, "Второй комментарий", now.minusHours(1));

        Collection<CommentDto> comments = commentPublicService.getEventComments(event.getId(), 0, 10, "ASC");

        assertThat(comments).hasSize(2);

        List<CommentDto> commentList = comments.stream().toList();

        assertThat(commentList.get(0).getId()).isEqualTo(comment1.getId());
        assertThat(commentList.get(0).getText()).isEqualTo("Первый комментарий");
        assertThat(commentList.get(1).getId()).isEqualTo(comment2.getId());
        assertThat(commentList.get(1).getText()).isEqualTo("Второй комментарий");
    }

    @Test
    void getEventComments_Success_Pagination() {
        Event event = createEvent(EventState.PUBLISHED, true);
        User author = createUser("Ivan");

        LocalDateTime now = LocalDateTime.now();
        createComment(author, event, "Первый комментарий", now.minusHours(3));
        Comment comment2 = createComment(author, event, "Второй комментарий", now.minusHours(2));
        createComment(author, event, "Третий комментарий", now.minusHours(1));


        Collection<CommentDto> comments = commentPublicService.getEventComments(event.getId(), 1, 1, "DESC");

        assertThat(comments).hasSize(1);
        CommentDto dto = comments.iterator().next();
        assertThat(dto.getId()).isEqualTo(comment2.getId());
        assertThat(dto.getText()).isEqualTo("Второй комментарий");
    }

    @Test
    void getEventComments_Success_EmptyList() {
        Event event = createEvent(EventState.PUBLISHED, true);

        Collection<CommentDto> comments = commentPublicService.getEventComments(event.getId(), 0, 10, "DESC");

        assertThat(comments).isEmpty();
    }

    @Test
    void getEventComments_EventNotFound_ThrowsNotFoundException() {
        assertThatThrownBy(() -> commentPublicService.getEventComments(999L, 0, 10, "DESC"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Событие с id=999 не найдено");
    }

    @Test
    void getEventComments_EventPending_ThrowsNotFoundException() {
        Event pendingEvent = createEvent(EventState.PENDING, true);

        assertThatThrownBy(() -> commentPublicService.getEventComments(pendingEvent.getId(), 0, 10, "DESC"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Событие с id=" + pendingEvent.getId() + " не опубликовано");
    }

    @Test
    void getEventComments_EventCanceled_ThrowsNotFoundException() {
        Event canceledEvent = createEvent(EventState.CANCELED, true);

        assertThatThrownBy(() -> commentPublicService.getEventComments(canceledEvent.getId(), 0, 10, "DESC"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Событие с id=" + canceledEvent.getId() + " не опубликовано");
    }

}