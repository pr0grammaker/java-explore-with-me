package ru.practicum.hits;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EndpointHitService {
    EndpointHitDto save(EndpointHitDto endpointHitDto);

    Collection<ViewStats> get(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
