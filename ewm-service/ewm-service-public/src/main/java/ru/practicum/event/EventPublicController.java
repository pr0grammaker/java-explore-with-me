package ru.practicum.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPublicController {
    private final EventPublicService service;

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
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok().body(
                service.getAllEvents(text, categories, paid, rangeStart, rangeEnd,
                        onlyAvailable, sort, from, size, request));
    }

    @GetMapping("{id}")
    public ResponseEntity<EventFullDto> getEventById(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok().body(service.getEventById(id, request));
    }

    @GetMapping("/some/path/{id}")
    public void logIPAndPath(@PathVariable long id, HttpServletRequest request) {
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());
    }

}
