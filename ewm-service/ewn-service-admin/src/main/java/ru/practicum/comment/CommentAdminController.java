package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class CommentAdminController {

    private final CommentAdminService commentAdminService;

    @GetMapping
    public ResponseEntity<Collection<CommentDto>> searchComments(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> events,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentAdminService.searchComments(users, events, text, from, size));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteCommentByAdmin(
            @PathVariable("commentId") Long commentId
    ) {
        commentAdminService.deleteCommentByAdmin(commentId);
        return ResponseEntity.noContent().build();
    }
}
