package ru.practicum.hits;

import java.util.Collection;
import java.util.List;

public interface EndpointHitService {
    EndpointHitDto save(EndpointHitDto endpointHitDto);

    Collection<ViewStats> get(String start, String end, List<String> uris, boolean unique);
}
