package ru.practicum.hits;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
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
        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        List<String> formattedUris = (uris != null)
                ? uris.stream()
                .filter(u -> u != null && !u.isBlank())
                .flatMap(u -> java.util.Arrays.stream(u.split(",")))
                .map(String::trim)
                .toList()
                : Collections.emptyList();

        boolean hasUris = !formattedUris.isEmpty();

        if (unique) {
            return hasUris
                    ? endpointHitRepository.findUniqueStatsByUris(start, end, formattedUris)
                    : endpointHitRepository.findAllUniqueStats(start, end);
        } else {
            return hasUris
                    ? endpointHitRepository.findStatsByUris(start, end, formattedUris)
                    : endpointHitRepository.findAllStats(start, end);
        }
    }
}
