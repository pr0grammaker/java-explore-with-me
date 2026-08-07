package ru.practicum.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.http.client.EndpointHttpClient;
import ru.practicum.participationrequest.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventPublicService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final EventMapper eventMapper;
    private final EndpointHttpClient endpointHttpClient;

    @Override
    public Collection<EventFullDto> getAllEvents(
            String text, List<Long> categories, Boolean paid, String rangeStart,
            String rangeEnd, Boolean onlyAvailable, String sort, int from, int size,
            HttpServletRequest request) {

        String searchText = (text != null && !text.isBlank()) ? text : null;
        List<Long> categoryIds = (categories != null && !categories.isEmpty()) ? categories : null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime start = (rangeStart != null && !rangeStart.isBlank())
                ? LocalDateTime.parse(rangeStart, formatter)
                : null;

        LocalDateTime end = (rangeEnd != null && !rangeEnd.isBlank())
                ? LocalDateTime.parse(rangeEnd, formatter)
                : null;

        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже конечной даты");
        }

        Sort sortBy = (sort != null && sort.equalsIgnoreCase("VIEWS"))
                ? Sort.by("views").descending()
                : Sort.by("eventDate").ascending();

        int page = (size > 0) ? (from / size) : 0;
        int limit = (size > 0) ? size : 10;
        Pageable pageable = PageRequest.of(page, limit, sortBy);

        Page<Event> events;
        if (start != null && end != null) {
            events = eventRepository.findAllBetween(searchText, categoryIds, paid, start, end, pageable);
        } else if (start != null) {
            events = eventRepository.findAllAfter(searchText, categoryIds, paid, start, pageable);
        } else if (end != null) {
            events = eventRepository.findAllBefore(searchText, categoryIds, paid, end, pageable);
        } else {
            events = eventRepository.findAllAfter(searchText, categoryIds, paid, LocalDateTime.now(), pageable);
        }

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

        try {
            EndpointHitDto endpoint = EndpointHitDto.builder()
                    .app("ewm-service-public")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();

            endpointHttpClient.saveHit(endpoint);
        } catch (Exception e) {
            log.error("Failed to send hit to stats-service: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event find = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие по id=%d не найдено".formatted(id)));

        if (!find.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие должно быть опубликовано");
        }

        long confirmedRequests = participationRequestRepository.countByEventIdAndStatus(
                find.getId(), RequestStatus.CONFIRMED);

        try {
            endpointHttpClient.saveHit(EndpointHitDto.builder()
                    .app("ewm-service-public")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to send stats: {}", e.getMessage());
        }

        long views = getViewsCount(request.getRequestURI());

        return eventMapper.mapToEventFullDto(find, confirmedRequests, views);
    }

    private long getViewsCount(String uri) {
        try {
            LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
            LocalDateTime end = LocalDateTime.now().plusDays(1);

            ResponseEntity<Collection<ViewStats>> response = endpointHttpClient.getStats(
                    start,
                    end,
                    List.of(uri),
                    false
            );

            if (response != null && response.getBody() != null && !response.getBody().isEmpty()) {
                return response.getBody().iterator().next().getHits();
            }
        } catch (Exception e) {
            log.error("Ошибка при получении просмотров из stats-service: {}", e.getMessage());
        }
        return 0L;
    }

}
