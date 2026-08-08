package ru.practicum.http.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import ru.practicum.event.EventFullDto;

import java.util.Collection;
import java.util.List;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/events"
)
public interface EventPublicHttpClient {

    @GetExchange
    ResponseEntity<Collection<EventFullDto>> getAllEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetExchange("{id}")
    ResponseEntity<EventFullDto> getEventById(
            @PathVariable("id") Long id
    );
}
