package ru.practicum.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.hits.EndpointHit;
import ru.practicum.hits.EndpointHitService;
import ru.practicum.hits.EndpointMapper;
import ru.practicum.participationrequest.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventPublicService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final EventMapper eventMapper;
    private final EndpointHitService endpointHitService;
    private final EndpointMapper endpointMapper;

    @Override
    public Collection<EventFullDto> getAllEvents(
            String text, List<Long> categories, boolean paid, String rangeStart,
            String rangeEnd, boolean onlyAvailable, String sort, int from, int size,
            HttpServletRequest request) {

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;

        Sort sortBy = sort.equalsIgnoreCase("VIEWS")
                ? Sort.by("views").descending()
                : Sort.by("eventDate").ascending();

        Pageable pageable = PageRequest.of(from / size, size, sortBy);

        Page<Event> events = eventRepository.findAllWithFilters(text, categories, paid, start, end, pageable);

        List<EventFullDto> result = events.stream()
                .filter(e -> !onlyAvailable || e.getParticipantLimit() == 0 ||
                        participationRequestRepository.countByEventIdAndStatus(e.getId(),
                                RequestStatus.CONFIRMED) < e.getParticipantLimit())
                .map(e -> {
                    long confirmedRequests = participationRequestRepository.countByEventIdAndStatus(e.getId(),
                            RequestStatus.CONFIRMED);
                    return eventMapper.mapToEventFullDto(e, confirmedRequests, e.getViews());
                })
                .toList();

        EndpointHit endpoint = EndpointHit.builder()
                .app("ewm-service-public")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();


        endpointHitService.save(endpointMapper.mapToEndpointHitDto(endpoint));
        return result;
    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event find = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие по id=%d не найдено".formatted(id)));

        if (!find.getState().equals(EventState.PUBLISHED)) {
            throw new InvalidEventOperationException("Событие должно быть опубликовано");
        }

        long confirmedRequests = participationRequestRepository.countByEventIdAndStatus(
                find.getId(), RequestStatus.CONFIRMED);

        EndpointHit endpoint = EndpointHit.builder()
                .app("ewm-service-public")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        endpointHitService.save(endpointMapper.mapToEndpointHitDto(endpoint));

        return eventMapper.mapToEventFullDto(find, confirmedRequests, find.getViews());
    }

}
