package ru.practicum.participationrequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.http.client.ParticipationRequestHttpClient;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class ParticipationRequestController {

    private final ParticipationRequestHttpClient participationRequestHttpClient;

    @GetMapping
    public ResponseEntity<Collection<ParticipationRequestDto>> getParticipationRequestsByUserId(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok().body(participationRequestHttpClient
                .getParticipationRequestsByUserId(userId).getBody());
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> createParticipationRequest(
            @PathVariable("userId") Long userId,
            @RequestParam("eventId") Long eventId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(participationRequestHttpClient.createParticipationRequest(userId, eventId).getBody());
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> updateParticipationRequest(
            @PathVariable("userId") Long userId,
            @PathVariable("requestId") Long requestId
    ) {
        return ResponseEntity.ok().body(participationRequestHttpClient
                .updateParticipationRequest(userId, requestId).getBody());
    }
}
