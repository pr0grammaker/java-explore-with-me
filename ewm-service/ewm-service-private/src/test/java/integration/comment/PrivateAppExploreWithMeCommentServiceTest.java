package integration.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.PrivateAppExploreWithMe;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.comment.*;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.event.Location;
import ru.practicum.exceptions.ConditionsNotMetException;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = PrivateAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
@Transactional
public class PrivateAppExploreWithMeCommentServiceTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentPrivateService commentPrivateService;

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

    @Test
    void addComment_Success() {
        Event event = createEvent(EventState.PUBLISHED, true);
        User author = createUser("Mike");

        CommentCreateDto createDto = new CommentCreateDto("Было круто надо бы еще раз собраться!");

        CommentDto commentDto = commentPrivateService.addComment(author.getId(), event.getId(), createDto);

        assertThat(commentDto).isNotNull();
        assertThat(commentDto.getId()).isNotNull();
        assertThat(commentDto.getText()).isEqualTo("Было круто надо бы еще раз собраться!");
        assertThat(commentDto.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(commentDto.getEventId()).isEqualTo(event.getId());

        // Проверяем инкремент счетчика в БД
        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getCommentsCount()).isEqualTo(1L);
    }

    @Test
    void addComment_UserNotFound_ThrowsNotFoundException() {
        Event event = createEvent(EventState.PUBLISHED, true);
        Long nonExistentUserId = 999L;
        CommentCreateDto createDto = CommentCreateDto.builder().text("Текст").build();

        assertThatThrownBy(() -> commentPrivateService.addComment(nonExistentUserId, event.getId(), createDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id=999 не найден");
    }

    @Test
    void addComment_EventNotFound_ThrowsNotFoundException() {
        User author = createUser("Mike");
        Long nonExistentEventId = 999L;
        CommentCreateDto createDto = CommentCreateDto.builder().text("Текст").build();

        assertThatThrownBy(() -> commentPrivateService.addComment(author.getId(), nonExistentEventId, createDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Событие с id=999 не найдено");
    }

    @Test
    void addComment_UnpublishedEvent_ThrowsInvalidEventOperationException() {
        Event event = createEvent(EventState.PENDING, true);
        User author = createUser("Mike");
        CommentCreateDto createDto = CommentCreateDto.builder().text("Текст").build();

        assertThatThrownBy(() -> commentPrivateService.addComment(author.getId(), event.getId(), createDto))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Нельзя добавить комментарий к неопубликованному событию");
    }

    @Test
    void addComment_CommentsDisabled_ThrowsConditionsNotMetException() {
        Event event = createEvent(EventState.PUBLISHED, false); // allowComments = false
        User author = createUser("Mike");
        CommentCreateDto createDto = CommentCreateDto.builder().text("Текст").build();

        assertThatThrownBy(() -> commentPrivateService.addComment(author.getId(), event.getId(), createDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessage("Нельзя добавить комментарий к событию у которого они отключены");
    }

    @Test
    void updateComment_Success() {
        Event event = createEvent(EventState.PUBLISHED, true);
        User author = createUser("Mike");
        CommentDto original = commentPrivateService.addComment(author.getId(), event.getId(),
                CommentCreateDto.builder().text("Старый текст").build());

        CommentUpdateDto updateDto = CommentUpdateDto.builder()
                .text("Обновленный текст")
                .build();

        CommentDto updated = commentPrivateService.updateComment(author.getId(), original.getId(), updateDto);

        assertThat(updated.getText()).isEqualTo("Обновленный текст");
        assertThat(updated.getUpdatedOn()).isNotNull();
    }

    @Test
    void updateComment_UserNotFound_ThrowsNotFoundException() {
        Long nonExistentUserId = 999L;
        CommentUpdateDto updateDto = CommentUpdateDto.builder().text("Обновленный текст").build();

        assertThatThrownBy(() -> commentPrivateService.updateComment(nonExistentUserId, 1L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id=999 не найден");
    }

    @Test
    void updateComment_CommentNotFound_ThrowsNotFoundException() {
        User author = createUser("Mike");
        Long nonExistentCommentId = 999L;
        CommentUpdateDto updateDto = CommentUpdateDto.builder().text("Обновленный текст").build();

        assertThatThrownBy(() -> commentPrivateService.updateComment(author.getId(), nonExistentCommentId, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Комментарий пользователя с id=999 не найден");
    }

    @Test
    void updateComment_NotAuthor_ThrowsConditionsNotMetException() {
        Event event = createEvent(EventState.PUBLISHED, true);
        User author = createUser("Mike");
        User stranger = createUser("John");

        CommentDto commentDto = commentPrivateService.addComment(author.getId(), event.getId(),
                CommentCreateDto.builder().text("Первоначальный текст").build());

        CommentUpdateDto updateDto = CommentUpdateDto.builder().text("Попытка чужого редактирования").build();

        assertThatThrownBy(() -> commentPrivateService.updateComment(stranger.getId(), commentDto.getId(), updateDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessage("Пользователь с id=" + stranger.getId() + " не является автором комментария");
    }

    @Test
    void deleteComment_UserNotFound_ThrowsNotFoundException() {
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> commentPrivateService.deleteComment(nonExistentUserId, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id=999 не найден");
    }

    @Test
    void deleteComment_CommentNotFound_ThrowsNotFoundException() {
        User author = createUser("Mike");
        Long nonExistentCommentId = 999L;

        assertThatThrownBy(() -> commentPrivateService.deleteComment(author.getId(), nonExistentCommentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Комментарий пользователя с id=999 не найден");
    }

    @Test
    void deleteComment_NotAuthor_ThrowsConditionsNotMetException() {
        Event event = createEvent(EventState.PUBLISHED, true);
        User author = createUser("Mike");
        User stranger = createUser("John");

        CommentDto commentDto = commentPrivateService.addComment(author.getId(), event.getId(),
                CommentCreateDto.builder().text("Текст для удаления").build());

        assertThatThrownBy(() -> commentPrivateService.deleteComment(stranger.getId(), commentDto.getId()))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessage("Пользователь с id=" + stranger.getId() + " не является автором комментария");
    }

    @Test
    void deleteComment_Success() {
        Event event = createEvent(EventState.PUBLISHED, true);
        User author = createUser("Mike");
        CommentDto comment = commentPrivateService.addComment(author.getId(), event.getId(),
                CommentCreateDto.builder().text("Удалите меня").build());

        commentPrivateService.deleteComment(author.getId(), comment.getId());

        assertThat(commentRepository.findById(comment.getId())).isEmpty();

        // Проверяем декремент счетчика
        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getCommentsCount()).isEqualTo(0L);
    }

    @Test
    void getUserComments_Success() {
        Event event = createEvent(EventState.PUBLISHED, true);
        User author = createUser("Mike");

        commentPrivateService.addComment(author.getId(), event.getId(), CommentCreateDto.builder().text("Коммент 1").build());
        commentPrivateService.addComment(author.getId(), event.getId(), CommentCreateDto.builder().text("Коммент 2").build());

        Collection<CommentDto> comments = commentPrivateService.getUserComments(author.getId(), 0, 10);

        assertThat(comments).hasSize(2);
    }

    @Test
    void getUserComments_UserNotFound_ThrowsNotFoundException() {
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> commentPrivateService.getUserComments(nonExistentUserId, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id=999 не найден");
    }
}