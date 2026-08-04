package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventAdminController {

    private final EventAdminService eventAdminService;

    @GetMapping
    public ResponseEntity<Collection<EventFullDto>> getEvents(
            @RequestParam List<Integer> users,
            @RequestParam List<String> states,
            @RequestParam List<Integer> categories,
            @RequestParam String rangeStart,
            @RequestParam String rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size

    ) {
        return ResponseEntity.ok().body(eventAdminService
                .getEvents(users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping("{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(
            @PathVariable Long eventId,
            @RequestBody UpdateEventAdminRequest updateEventAdminRequest
    ) {
        return ResponseEntity.ok().body(eventAdminService.updateEvent(eventId, updateEventAdminRequest));
    }
}
