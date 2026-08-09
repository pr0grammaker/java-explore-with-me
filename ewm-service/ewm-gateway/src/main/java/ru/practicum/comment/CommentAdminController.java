package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.http.client.CommentAdminHttpClient;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class CommentAdminController {

    private final CommentAdminHttpClient commentAdminHttpClient;

    @GetMapping
    public ResponseEntity<Collection<CommentDto>> searchComments(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> events,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentAdminHttpClient.searchComments(users, events, text, from, size).getBody());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteCommentByAdmin(
            @PathVariable("commentId") Long commentId
    ) {
        commentAdminHttpClient.deleteCommentByAdmin(commentId);
        return ResponseEntity.noContent().build();
    }
}
