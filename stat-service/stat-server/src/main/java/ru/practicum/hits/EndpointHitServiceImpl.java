package ru.practicum.hits;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitRepository endpointHitRepository;
    private final EndpointMapper mapper;


    @Override
    public EndpointHitDto save(EndpointHitDto endpointHitDto) {
        EndpointHit entity = mapper.mapToEndpointHit(endpointHitDto);
        entity = endpointHitRepository.save(entity);
        return mapper.mapToEndpointHitDto(entity);
    }

    @Override
    public Collection<ViewStats> get(String start, String end, List<String> uris, boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);

        if (unique) {
            return endpointHitRepository.findUniqueStats(startDate, endDate, uris);
        } else {
            return endpointHitRepository.findStats(startDate, endDate, uris);
        }
    }
}
