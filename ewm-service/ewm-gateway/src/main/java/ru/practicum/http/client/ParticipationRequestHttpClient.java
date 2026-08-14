package ru.practicum.http.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import ru.practicum.participationrequest.ParticipationRequestDto;

import java.util.Collection;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/users/{userId}/requests"
)
public interface ParticipationRequestHttpClient {

    @GetExchange
    ResponseEntity<Collection<ParticipationRequestDto>> getParticipationRequestsByUserId(
            @PathVariable("userId") Long userId
    );

    @PostExchange
    ResponseEntity<ParticipationRequestDto> createParticipationRequest(
            @PathVariable("userId") Long userId,
            @RequestParam("eventId") Long eventId
    );

    @PatchExchange("/{requestId}/cancel")
    ResponseEntity<ParticipationRequestDto> updateParticipationRequest(
            @PathVariable("userId") Long userId,
            @PathVariable("requestId") Long requestId
    );
}
