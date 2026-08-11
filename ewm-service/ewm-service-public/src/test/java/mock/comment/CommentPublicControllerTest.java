package mock.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.PublicAppExploreWithMe;
import ru.practicum.comment.Comment;
import ru.practicum.comment.CommentDto;
import ru.practicum.comment.CommentPublicController;
import ru.practicum.comment.CommentPublicService;
import ru.practicum.event.Event;
import ru.practicum.event.EventState;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CommentPublicController.class)
@ContextConfiguration(classes = PublicAppExploreWithMe.class)
public class CommentPublicControllerTest {

    @MockBean
    private CommentPublicService commentPublicService;

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
    void getEventComments_Success() throws Exception {
        when(commentPublicService.getEventComments(eq(10L), anyInt(), anyInt(), anyString()))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/events/{eventId}/comments", 10L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(commentDto.getId()))
                .andExpect(jsonPath("$[0].text").value(commentDto.getText()))
                .andExpect(jsonPath("$[0].eventId").value(commentDto.getEventId()))
                .andExpect(jsonPath("$[0].author.id").value(commentDto.getAuthor().getId()))
                .andExpect(jsonPath("$[0].author.name").value(commentDto.getAuthor().getName()));
    }

    @Test
    void getEventComments_Success_WithCustomParams() throws Exception {
        when(commentPublicService.getEventComments(eq(10L), eq(0), eq(5), eq("ASC")))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/events/{eventId}/comments", 10L)
                        .param("from", "0")
                        .param("size", "5")
                        .param("sort", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(commentPublicService).getEventComments(10L, 0, 5, "ASC");
    }

    @Test
    void getEventComments_Success_EmptyList() throws Exception {
        when(commentPublicService.getEventComments(anyLong(), anyInt(), anyInt(), anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/events/{eventId}/comments", 10L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEventComments_EventNotFound_Returns404() throws Exception {
        when(commentPublicService.getEventComments(eq(999L), anyInt(), anyInt(), anyString()))
                .thenThrow(new NotFoundException("Событие с id=999 не найдено"));

        mockMvc.perform(get("/events/{eventId}/comments", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEventComments_EventNotPublished_Returns404() throws Exception {
        when(commentPublicService.getEventComments(eq(10L), anyInt(), anyInt(), anyString()))
                .thenThrow(new NotFoundException("Событие с id=10 не опубликовано"));

        mockMvc.perform(get("/events/{eventId}/comments", 10L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


}



