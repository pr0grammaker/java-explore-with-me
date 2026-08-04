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
            @RequestParam String text,
            @RequestParam List<Long> categories,
            @RequestParam boolean paid,
            @RequestParam String rangeStart,
            @RequestParam String rangeEnd,
            @RequestParam boolean onlyAvailable,
            @RequestParam String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetExchange("{id}")
    ResponseEntity<EventFullDto> getEventById(
            @PathVariable("id") Long id
    );
}
