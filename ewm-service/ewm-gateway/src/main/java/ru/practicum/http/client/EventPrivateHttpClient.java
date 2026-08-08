package ru.practicum.http.client;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import ru.practicum.event.*;
import ru.practicum.participationrequest.ParticipationRequestDto;

import java.util.Collection;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/users/{userId}/events"
)
public interface EventPrivateHttpClient {

    @GetExchange
    ResponseEntity<Collection<EventFullDto>> getEventsByUser(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @PostExchange
    ResponseEntity<EventFullDto> createEvent(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody EventShortDto eventShortDto
    );

    @GetExchange("{eventId}")
    ResponseEntity<EventFullDto> getUserEventById(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    );

    @PatchExchange("{eventId}")
    ResponseEntity<EventFullDto> updateUserEventById(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest
    );

    @GetExchange("{eventId}/requests")
    ResponseEntity<Collection<ParticipationRequestDto>> getUserEventRequestsByUserId(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    );

    @PatchExchange("{eventId}/requests")
    ResponseEntity<EventRequestStatusUpdateResult> updateUserEventRequestsByUserId(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest
    );

}
