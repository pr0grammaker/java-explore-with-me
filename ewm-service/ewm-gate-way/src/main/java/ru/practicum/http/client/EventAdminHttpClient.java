package ru.practicum.http.client;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import ru.practicum.event.EventFullDto;
import ru.practicum.event.UpdateEventAdminRequest;

import java.util.Collection;
import java.util.List;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/admin/events"
)
public interface EventAdminHttpClient {

    @GetExchange
    ResponseEntity<Collection<EventFullDto>> getEvents(
            @RequestParam List<Integer> users,
            @RequestParam List<String> states,
            @RequestParam List<Integer> categories,
            @RequestParam String rangeStart,
            @RequestParam String rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size

    );

    @PatchExchange("{eventId}")
    ResponseEntity<EventFullDto> updateEvent(
            @PathVariable Long eventId,
            @RequestBody UpdateEventAdminRequest updateEventAdminRequest
    );
}
