package ru.practicum.event;

import ru.practicum.participationrequest.ParticipationRequestDto;

import java.util.Collection;

public interface EventService {

    Collection<EventFullDto> getEventsByUser(Long userId, int from, int size);

    EventFullDto addEvent(Long userId, EventShortDto eventShortDto);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto updateUserEventById(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    Collection<ParticipationRequestDto> getUserEventRequestsByUserId(Long userId, Long eventId);

//    EventRequestStatusUpdateResult updateUserEventRequestsByUserId(
//            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}
