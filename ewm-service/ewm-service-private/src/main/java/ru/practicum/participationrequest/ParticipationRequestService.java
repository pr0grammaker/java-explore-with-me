package ru.practicum.participationrequest;

import java.util.Collection;

public interface ParticipationRequestService {
    Collection<ParticipationRequestDto> getParticipationRequestsByUserId(Long userId);

    ParticipationRequestDto createParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto updateParticipationRequest(Long userId, Long requestId);
}
