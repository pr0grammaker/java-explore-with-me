package ru.practicum.event;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.List;

public interface EventPublicService {
    Collection<EventFullDto> getAllEvents(String text, List<Long> categories,
                                          boolean paid, String rangeStart,
                                          String rangeEnd, boolean onlyAvailable,
                                          String sort, int from, int size, HttpServletRequest request);

    EventFullDto getEventById(Long id, HttpServletRequest request);
}
