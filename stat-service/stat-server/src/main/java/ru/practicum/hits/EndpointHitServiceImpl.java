package ru.practicum.hits;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitRepository endpointHitRepository;
    private final EndpointMapper mapper;

    @Override
    @Transactional
    public EndpointHitDto save(EndpointHitDto endpointHitDto) {
        EndpointHit entity = mapper.mapToEndpointHit(endpointHitDto);
        entity = endpointHitRepository.save(entity);
        return mapper.mapToEndpointHitDto(entity);
    }

    @Override
    public Collection<ViewStats> get(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (unique) {
            return endpointHitRepository.findUniqueStats(start, end, uris);
        } else {
            return endpointHitRepository.findStats(start, end, uris);
        }
    }
}
