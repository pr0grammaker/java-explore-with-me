package ru.practicum.http.client;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;
import ru.practicum.comment.CommentCreateDto;
import ru.practicum.comment.CommentDto;
import ru.practicum.comment.CommentUpdateDto;

import java.util.Collection;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/users/{userId}"
)
public interface CommentPrivateHttpClient {
    @PostExchange("/events/{eventId}/comments")
     ResponseEntity<CommentDto> addComment(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody CommentCreateDto commentCreateDto

    );

    @PatchExchange("/comments/{commentId}")
     ResponseEntity<CommentDto> updateComment(
            @PathVariable("userId") Long userId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentUpdateDto commentUpdateDto
    );

    @DeleteExchange("/comments/{commentId}")
     ResponseEntity<Void> deleteComment(
            @PathVariable("userId") Long userId,
            @PathVariable("commentId") Long commentId
    );

    @GetExchange("/comments")
     ResponseEntity<Collection<CommentDto>> getUserComments(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );
}
