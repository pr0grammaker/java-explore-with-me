package ru.practicum.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class CommentPrivateController {

    private final CommentPrivateService commentPrivateService;

    @PostMapping("/events/{eventId}/comments")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody CommentCreateDto commentCreateDto

    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentPrivateService.addComment(userId, eventId, commentCreateDto));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable("userId") Long userId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentUpdateDto commentUpdateDto
    ) {
        return ResponseEntity.ok()
                .body(commentPrivateService.updateComment(userId, commentId, commentUpdateDto));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("userId") Long userId,
            @PathVariable("commentId") Long commentId
    ) {
        commentPrivateService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments")
    public ResponseEntity<Collection<CommentDto>> getUserComments(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok()
                .body(commentPrivateService.getUserComments(userId, from, size));
    }


}
