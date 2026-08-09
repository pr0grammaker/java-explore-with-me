package ru.practicum.event;

import java.util.Collection;
import java.util.List;

public interface EventAdminService {
    Collection<EventFullDto> getEvents(List<Long> users, List<String> states,
                                       List<Long> categories, String rangeStart,
                                       String rangeEnd, int from, int size);


    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);
}
