package ru.practicum.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.participationrequest.ParticipationRequestDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Collection<EventFullDto>> getEventsByUser(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(eventService.getEventsByUser(userId, from, size));
    }

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody EventShortDto eventShortDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addEvent(userId, eventShortDto));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getUserEventById(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        return ResponseEntity.ok().body(eventService.getUserEventById(userId, eventId));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateUserEventById(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody(required = false) UpdateEventUserRequest updateEventUserRequest
    ) {
        return ResponseEntity.ok().body(eventService.updateUserEventById(userId, eventId, updateEventUserRequest));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<Collection<ParticipationRequestDto>> getUserEventRequestsByUserId(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        return ResponseEntity.ok().body(eventService.getUserEventRequestsByUserId(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateUserEventRequestsByUserId(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody(required = false) EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest
    ) {
        return ResponseEntity.ok().body(eventService
                .updateUserEventRequestsByUserId(userId, eventId, eventRequestStatusUpdateRequest));
    }
}
