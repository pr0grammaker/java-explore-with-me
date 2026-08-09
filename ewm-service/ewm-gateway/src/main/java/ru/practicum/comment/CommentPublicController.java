package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.http.client.CommentPublicHttpClient;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/comments")
public class CommentPublicController {

    private final CommentPublicHttpClient commentPublicHttpClient;

    @GetMapping
    public ResponseEntity<Collection<CommentDto>> getEventComments(
            @PathVariable("eventId") Long eventId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sort
    ) {
        return ResponseEntity.ok()
                .body(commentPublicHttpClient.getEventComments(eventId, from, size, sort).getBody());
    }
}
