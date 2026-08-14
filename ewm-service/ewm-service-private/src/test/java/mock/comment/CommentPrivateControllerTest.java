package mock.comment;

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
import ru.practicum.comment.*;
import ru.practicum.event.Event;
import ru.practicum.event.EventState;
import ru.practicum.exceptions.ConditionsNotMetException;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CommentPrivateController.class)
@ContextConfiguration(classes = PrivateAppExploreWithMe.class)
public class CommentPrivateControllerTest {

    @MockBean
    private CommentPrivateService commentPrivateService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Event event;
    private Comment comment;
    private CommentDto commentDto;

    private CommentCreateDto commentCreateDto;
    private CommentUpdateDto commentUpdateDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Artem")
                .email("artem@example.com")
                .build();

        event = Event.builder()
                .id(10L)
                .title("Rock Concert")
                .state(EventState.PUBLISHED)
                .allowComments(true)
                .commentsCount(0L)
                .build();

        commentCreateDto = CommentCreateDto.builder()
                .text("Отличный концерт!")
                .build();

        commentUpdateDto = CommentUpdateDto.builder()
                .text("Обновленный текст комментария")
                .build();

        comment = Comment.builder()
                .id(100L)
                .text("Отличный концерт!")
                .author(user)
                .event(event)
                .createdOn(LocalDateTime.now())
                .build();

        commentDto = CommentDto.builder()
                .id(100L)
                .text("Отличный концерт!")
                .eventId(10L)
                .author(new UserShortDto(1L, "Artem"))
                .createdOn(comment.getCreatedOn())
                .build();
    }

    @Test
    void addComment_Success() throws Exception {
        when(commentPrivateService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/users/{userId}/events/{eventId}/comments", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.author.id").value(commentDto.getAuthor().getId()))
                .andExpect(jsonPath("$.author.name").value(commentDto.getAuthor().getName()))
                .andExpect(jsonPath("$.eventId").value(commentDto.getEventId()));
    }

    @Test
    void addComment_NotFound() throws Exception {
        when(commentPrivateService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenThrow(new NotFoundException("Событие с id=999 не найдено"));

        mockMvc.perform(post("/users/{userId}/events/{eventId}/comments", 1L, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_EventNotPublished() throws Exception {
        when(commentPrivateService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenThrow(new InvalidEventOperationException("Нельзя добавить комментарий к неопубликованному событию"));

        mockMvc.perform(post("/users/{userId}/events/{eventId}/comments", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void addComment_CommentsDisabled() throws Exception {
        when(commentPrivateService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenThrow(new ConditionsNotMetException("Нельзя добавить комментарий к событию у которого они отключены"));

        mockMvc.perform(post("/users/{userId}/events/{eventId}/comments", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_Success() throws Exception {
        CommentDto updatedDto = CommentDto.builder()
                .id(100L)
                .text("Обновленный текст комментария")
                .eventId(10L)
                .author(new UserShortDto(1L, "Artem"))
                .createdOn(comment.getCreatedOn())
                .updatedOn(LocalDateTime.now())
                .build();

        when(commentPrivateService.updateComment(anyLong(), anyLong(), any(CommentUpdateDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/users/{userId}/comments/{commentId}", 1L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.text").value("Обновленный текст комментария"))
                .andExpect(jsonPath("$.updatedOn").exists());
    }

    @Test
    void updateComment_NotFound() throws Exception {
        when(commentPrivateService.updateComment(anyLong(), anyLong(), any(CommentUpdateDto.class)))
                .thenThrow(new NotFoundException("Комментарий пользователя с id=999 не найден"));

        mockMvc.perform(patch("/users/{userId}/comments/{commentId}", 1L, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateComment_NotAuthor() throws Exception {
        when(commentPrivateService.updateComment(anyLong(), anyLong(), any(CommentUpdateDto.class)))
                .thenThrow(new ConditionsNotMetException("Пользователь с id=2 не является автором комментария"));

        mockMvc.perform(patch("/users/{userId}/comments/{commentId}", 2L, 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deleteComment_Success() throws Exception {
        doNothing().when(commentPrivateService).deleteComment(anyLong(), anyLong());

        mockMvc.perform(delete("/users/{userId}/comments/{commentId}", 1L, 100L))
                .andExpect(status().isNoContent());

        verify(commentPrivateService, times(1)).deleteComment(1L, 100L);
    }

    @Test
    void deleteComment_NotFound() throws Exception {
        doThrow(new NotFoundException("Комментарий пользователя с id=999 не найден"))
                .when(commentPrivateService).deleteComment(anyLong(), anyLong());

        mockMvc.perform(delete("/users/{userId}/comments/{commentId}", 1L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_NotAuthor() throws Exception {
        doThrow(new ConditionsNotMetException("Пользователь с id=2 не является автором комментария"))
                .when(commentPrivateService).deleteComment(anyLong(), anyLong());

        mockMvc.perform(delete("/users/{userId}/comments/{commentId}", 2L, 100L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserComments_Success() throws Exception {
        when(commentPrivateService.getUserComments(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/users/{userId}/comments", 1L)
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(commentDto.getId()))
                .andExpect(jsonPath("$[0].text").value(commentDto.getText()));
    }

    @Test
    void getUserComments_UserNotFound() throws Exception {
        when(commentPrivateService.getUserComments(anyLong(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(get("/users/{userId}/comments", 999L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }
}



