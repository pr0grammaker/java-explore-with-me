package mock.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.comment.Comment;
import ru.practicum.comment.CommentAdminController;
import ru.practicum.comment.CommentAdminService;
import ru.practicum.comment.CommentDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventState;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentAdminController.class)
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class CommentAdminControllerTest {

    @MockBean
    private CommentAdminService commentAdminService;

    @Autowired
    private MockMvc mockMvc;

    private User user;
    private Event event;
    private Comment comment;
    private CommentDto commentDto;

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
    void searchComments_Success_DefaultParams() throws Exception {
        when(commentAdminService.searchComments(eq(null), eq(null), eq(null), eq(0), eq(10)))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/admin/comments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(commentDto.getId()))
                .andExpect(jsonPath("$[0].text").value(commentDto.getText()))
                .andExpect(jsonPath("$[0].eventId").value(commentDto.getEventId()))
                .andExpect(jsonPath("$[0].author.id").value(commentDto.getAuthor().getId()))
                .andExpect(jsonPath("$[0].author.name").value(commentDto.getAuthor().getName()));

        verify(commentAdminService).searchComments(null, null, null, 0, 10);
    }

    @Test
    void searchComments_Success_WithAllParams() throws Exception {
        List<Long> users = List.of(1L, 2L);
        List<Long> events = List.of(10L);
        String text = "концерт";

        when(commentAdminService.searchComments(eq(users), eq(events), eq(text), eq(0), eq(5)))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/admin/comments")
                        .param("users", "1", "2")
                        .param("events", "10")
                        .param("text", text)
                        .param("from", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(commentDto.getId()));

        verify(commentAdminService).searchComments(users, events, text, 0, 5);
    }

    @Test
    void searchComments_Success_EmptyResult() throws Exception {
        when(commentAdminService.searchComments(eq(null), eq(null), eq("несуществующий текст"), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/comments")
                        .param("text", "несуществующий текст")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteCommentByAdmin_Success() throws Exception {
        doNothing().when(commentAdminService).deleteCommentByAdmin(100L);

        mockMvc.perform(delete("/admin/comments/{commentId}", 100L))
                .andExpect(status().isNoContent());

        verify(commentAdminService).deleteCommentByAdmin(100L);
    }

    @Test
    void deleteCommentByAdmin_NotFound_Returns404() throws Exception {
        doThrow(new NotFoundException("Комментарий пользователя с id=999 не найден"))
                .when(commentAdminService).deleteCommentByAdmin(999L);

        mockMvc.perform(delete("/admin/comments/{commentId}", 999L))
                .andExpect(status().isNotFound());

        verify(commentAdminService).deleteCommentByAdmin(999L);
    }


}
