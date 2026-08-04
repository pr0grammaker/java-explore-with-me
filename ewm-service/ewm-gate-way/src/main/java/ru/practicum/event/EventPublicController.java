package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.http.client.EventPublicHttpClient;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPublicController {
    private final EventPublicHttpClient eventPublicHttpClient;

    @GetMapping
    public ResponseEntity<Collection<EventFullDto>> getAllEvents(
            @RequestParam String text,
            @RequestParam List<Long> categories,
            @RequestParam boolean paid,
            @RequestParam String rangeStart,
            @RequestParam String rangeEnd,
            @RequestParam boolean onlyAvailable,
            @RequestParam String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(
                eventPublicHttpClient.getAllEvents(text, categories, paid, rangeStart, rangeEnd,
                        onlyAvailable, sort, from, size).getBody());
    }

    @GetMapping("{id}")
    public ResponseEntity<EventFullDto> getEventById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok().body(eventPublicHttpClient.getEventById(id).getBody());
    }
}
