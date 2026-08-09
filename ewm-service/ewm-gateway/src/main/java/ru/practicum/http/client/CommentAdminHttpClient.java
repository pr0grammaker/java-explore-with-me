package ru.practicum.http.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import ru.practicum.comment.CommentDto;

import java.util.Collection;
import java.util.List;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/admin/comments"
)
public interface CommentAdminHttpClient {

    @GetExchange
    ResponseEntity<Collection<CommentDto>> searchComments(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> events,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @DeleteExchange("/{commentId}")
    ResponseEntity<Void> deleteCommentByAdmin(
            @PathVariable("commentId") Long commentId
    );
}
