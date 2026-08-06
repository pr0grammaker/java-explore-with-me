package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.participationrequest.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventAdminServiceImpl implements EventAdminService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final ParticipationRequestRepository participationRequestRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Collection<EventFullDto> getEvents(List<Long> users, List<String> states,
                                              List<Long> categories, String rangeStart,
                                              String rangeEnd, int from, int size) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime start = (rangeStart != null && !rangeStart.isBlank())
                ? LocalDateTime.parse(rangeStart, formatter)
                : null;

        LocalDateTime end = (rangeEnd != null && !rangeEnd.isBlank())
                ? LocalDateTime.parse(rangeEnd, formatter)
                : null;

        Pageable pageable = PageRequest.of(from / size, size);

        List<EventState> stateEnums = (states != null && !states.isEmpty())
                ? states.stream().map(EventState::valueOf).toList() : null;
        List<Long> userIds = (users != null && !users.isEmpty()) ? users : null;
        List<Long> categoryIds = (categories != null && !categories.isEmpty()) ? categories : null;

        Page<Event> events = eventRepository.findAllByAdminFilters(
                userIds,
                stateEnums,
                categoryIds,
                start,
                end,
                pageable
        );

        return events.stream()
                .map(e -> eventMapper.mapToEventFullDto(e,
                        participationRequestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED),
                        e.getViews()))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие по id=%d не найдено".formatted(eventId)));

        if (updateEventAdminRequest.getStateAction() == StateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new InvalidEventOperationException(
                        "Событие можно публиковать только если оно в состоянии ожидания публикации");
            }
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new InvalidEventOperationException(
                        "Дата начала события должна быть не ранее чем за час от даты публикации");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        if (updateEventAdminRequest.getStateAction() == StateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new InvalidEventOperationException("Событие нельзя отклонить, если оно уже опубликовано");
            }
            event.setState(EventState.CANCELED);
        }

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id = %d не найдена"
                            .formatted(updateEventAdminRequest.getCategory())));
            event.setCategory(category);
        }

        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            if (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, " +
                        "чем через два часа от текущего момента");
            } else {
                event.setEventDate(updateEventAdminRequest.getEventDate());
            }
        }

        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(updateEventAdminRequest.getLocation());
        }

        boolean exists = eventRepository.existsAnotherEventAtSamePlaceAndTime(
                event.getLocation().getLat(),
                event.getLocation().getLon(),
                event.getEventDate(),
                event.getId()
        );
        if (exists) {
            throw new InvalidEventOperationException(
                    "На указанной локации уже запланировано событие на это время"
            );
        }

        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }

        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }

        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        eventRepository.save(event);

        long confirmedRequests = participationRequestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        return eventMapper.mapToEventFullDto(event, confirmedRequests, event.getViews());
    }

}
