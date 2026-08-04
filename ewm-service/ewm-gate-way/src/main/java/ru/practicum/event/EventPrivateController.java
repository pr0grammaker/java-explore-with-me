package ru.practicum.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.http.client.EventPrivateHttpClient;
import ru.practicum.participationrequest.ParticipationRequestDto;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {
    private final EventPrivateHttpClient eventPrivateHttpClient;

    @GetMapping
    public ResponseEntity<Collection<EventFullDto>> getEventsByUser(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(eventPrivateHttpClient.getEventsByUser(userId, from, size).getBody());
    }

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody EventShortDto eventShortDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventPrivateHttpClient.createEvent(userId, eventShortDto).getBody());
    }

    @GetMapping("{eventId}")
    public ResponseEntity<EventFullDto> getUserEventById(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        return ResponseEntity.ok().body(eventPrivateHttpClient.getUserEventById(userId, eventId).getBody());
    }

    @PatchMapping("{eventId}")
    public ResponseEntity<EventFullDto> updateUserEventById(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest
    ) {
        return ResponseEntity.ok().body(eventPrivateHttpClient
                .updateUserEventById(userId, eventId, updateEventUserRequest).getBody());
    }

    @GetMapping("{eventId}/requests")
    public ResponseEntity<Collection<ParticipationRequestDto>> getUserEventRequestsByUserId(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        return ResponseEntity.ok()
                .body(eventPrivateHttpClient.getUserEventRequestsByUserId(userId, eventId).getBody());
    }

    @PatchMapping("{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateUserEventRequestsByUserId(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest
    ) {
        return ResponseEntity.ok().body(eventPrivateHttpClient
                .updateUserEventRequestsByUserId(userId, eventId, eventRequestStatusUpdateRequest).getBody());
    }
}
