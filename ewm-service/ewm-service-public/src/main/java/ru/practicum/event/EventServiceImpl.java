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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<Event> eventList = events.stream()
                .filter(e -> !onlyAvailable || e.getParticipantLimit() == 0 ||
                        participationRequestRepository.countByEventIdAndStatus(e.getId(),
                                RequestStatus.CONFIRMED) < e.getParticipantLimit())
                .toList();

        Map<String, Long> viewsMap = getViewsCountMap(eventList);

        List<EventFullDto> result = eventList.stream()
                .map(e -> {
                    long confirmedRequests = participationRequestRepository.countByEventIdAndStatus(
                            e.getId(), RequestStatus.CONFIRMED);
                    String uri = "/events/" + e.getId();
                    long views = viewsMap.getOrDefault(uri, 0L);

                    return eventMapper.mapToEventFullDto(e, confirmedRequests, views);
                })
                .toList();

        try {
            EndpointHitDto endpoint = EndpointHitDto.builder()
                    .app("ewm-main-service")
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

        try {
            endpointHttpClient.saveHit(EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to send stats: {}", e.getMessage());
        }

        long confirmedRequests = participationRequestRepository.countByEventIdAndStatus(
                find.getId(), RequestStatus.CONFIRMED);

        long views = getViewsCount(request.getRequestURI());

        return eventMapper.mapToEventFullDto(find, confirmedRequests, views);
    }

    private long getViewsCount(String uri) {
        try {
            LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2035, 1, 1, 0, 0, 0);

            ResponseEntity<Collection<ViewStats>> response = endpointHttpClient.getStats(
                    start,
                    end,
                    List.of(uri),
                    true
            );

            log.info("STATS RESPONSE: status={}, body={}", response.getStatusCode(), response.getBody());

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                return response.getBody().iterator().next().getHits();
            }
        } catch (Exception e) {
            log.error("Ошибка при получении просмотров из stats-service: {}", e.getMessage());
        }
        return 0L;
    }

    private Map<String, Long> getViewsCountMap(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        try {
            LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
            LocalDateTime end = LocalDateTime.now().plusDays(1);

            ResponseEntity<Collection<ViewStats>> response = endpointHttpClient.getStats(
                    start,
                    end,
                    uris,
                    true
            );

            if (response != null && response.getBody() != null) {
                return response.getBody().stream()
                        .collect(Collectors.toMap(
                                ViewStats::getUri,
                                ViewStats::getHits,
                                (v1, v2) -> v1
                        ));
            }
        } catch (Exception e) {
            log.error("Ошибка при получении массовой статистики из stats-service: {}", e.getMessage());
        }

        return Collections.emptyMap();
    }

}
