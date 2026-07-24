package ru.practicum.hits;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitRepository endpointHitRepository;
    private final EndpointMapper mapper;


    @Override
    public EndpointHitDto save(EndpointHitDto endpointHitDto) {
        return null;
    }

    @Override
    public Collection<ViewStats> get(String start, String end, List<String> uris, boolean unique) {
        return List.of();
    }
}
