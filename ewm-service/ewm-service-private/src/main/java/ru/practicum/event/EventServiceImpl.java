package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.exceptions.ConditionsNotMetException;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.participationrequest.ParticipationRequest;
import ru.practicum.participationrequest.ParticipationRequestDto;
import ru.practicum.participationrequest.ParticipationRequestMapper;
import ru.practicum.participationrequest.ParticipationRequestRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Override
    public Collection<EventFullDto> getEventsByUser(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createdOn").ascending());

        List<Event> events = eventRepository.findAllByUserId(userId, pageable);
        return events.stream()
                .map(eventMapper::mapToEventFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, EventShortDto eventShortDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = %d не найден".formatted(userId)));

        Category category = categoryRepository.findById(eventShortDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id = %d не найдена"
                        .formatted(eventShortDto.getCategory())));

        if (eventShortDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new InvalidEventOperationException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }

        Event event = Event.builder()
                .annotation(eventShortDto.getAnnotation())
                .category(category)
                .confirmedRequests(0L)
                .createdOn(LocalDateTime.now())
                .description(eventShortDto.getDescription())
                .eventDate(eventShortDto.getEventDate())
                .initiator(initiator)
                .location(eventShortDto.getLocation())
                .paid(eventShortDto.getPaid())
                .participantLimit(eventShortDto.getParticipantLimit())
                .publishedOn(null)
                .requestModeration(eventShortDto.getRequestModeration())
                .state(EventState.PENDING)
                .title(eventShortDto.getTitle())
                .views(0L)
                .build();

        boolean exists = eventRepository.existsByLocationLatAndLocationLonAndEventDate(
                event.getLocation().getLat(),
                event.getLocation().getLon(),
                event.getEventDate()
        );
        if (exists) {
            throw new InvalidEventOperationException("На указанной локации уже запланировано событие на это время");
        }


        eventRepository.save(event);
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = %d не найден".formatted(userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = %d не найдено".formatted(eventId)));

        if (!userId.equals(event.getInitiator().getId())) {
            throw new ConditionsNotMetException("Пользователь с id = %d не является инициатором события id = %d"
                    .formatted(userId, eventId));
        }

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEventById(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = %d не найден".formatted(userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = %d не найдено".formatted(eventId)));

        if (!userId.equals(event.getInitiator().getId())) {
            throw new InvalidEventOperationException("Пользователь не является инициатором события");
        }

        if (event.getState() != EventState.CANCELED && event.getState() != EventState.PENDING) {
            throw new InvalidEventOperationException(
                    "Изменить можно только отмененные события или события в состоянии ожидания модерации"
            );
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id = %d не найдена"
                            .formatted(updateEventUserRequest.getCategory())));
            event.setCategory(category);
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConditionsNotMetException("Дата и время на которые намечено событие не может быть раньше, " +
                        "чем через два часа от текущего момента");
            } else {
                event.setEventDate(updateEventUserRequest.getEventDate());
            }
        }

        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(updateEventUserRequest.getLocation());
        }

        boolean exists = eventRepository.existsAnotherEventAtSamePlaceAndTime(
                event.getLocation().getLat(),
                event.getLocation().getLon(),
                event.getEventDate(),
                eventId
        );
        if (exists) {
            throw new InvalidEventOperationException("На указанной локации уже запланировано событие на это время");
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                default -> throw new InvalidEventOperationException(
                        "Недопустимое действие для изменения состояния события"
                );
            }
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        eventRepository.save(event);
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public Collection<ParticipationRequestDto> getUserEventRequestsByUserId(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = %d не найден".formatted(userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = %d не найдено".formatted(eventId)));

        if (!userId.equals(event.getInitiator().getId())) {
            throw new ConditionsNotMetException("Пользователь с id = %d не является инициатором события id = %d"
                    .formatted(userId, eventId));
        }

        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findRequestsByUserEvent(userId, eventId);

        return participationRequests.stream()
                .map(participationRequestMapper::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateUserEventRequestsByUserId(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest updateRequest) {

        if (updateRequest == null || updateRequest.getRequestIds() == null || updateRequest.getRequestIds().isEmpty()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(List.of())
                    .build();
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = %d не найден".formatted(userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = %d не найдено".formatted(eventId)));

        long confirmedCount = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (updateRequest.getStatus() == RequestStatus.CONFIRMED
                && event.getParticipantLimit() > 0
                && confirmedCount >= event.getParticipantLimit()) {
            throw new InvalidEventOperationException("Нельзя подтвердить заявку, лимит участников уже достигнут");
        }

        List<ParticipationRequest> requests = participationRequestRepository
                .findAllById(updateRequest.getRequestIds());

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new InvalidEventOperationException("Заявка id=%d не относится к событию id=%d"
                        .formatted(request.getId(), eventId));
            }

            if (request.getStatus() != RequestStatus.PENDING) {
                throw new InvalidEventOperationException(
                        "Статус можно изменить только у заявок, находящихся в состоянии ожидания"
                );
            }

            if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
                if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                    throw new InvalidEventOperationException("Нельзя подтвердить заявку, лимит участников уже достигнут");
                }
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
                participationRequestRepository.save(request);
                confirmed.add(participationRequestMapper.mapToDto(request));

            } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                participationRequestRepository.save(request);
                rejected.add(participationRequestMapper.mapToDto(request));
            }
        }

        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            List<ParticipationRequest> pendingRequests = participationRequestRepository
                    .findByEventIdAndStatus(eventId, RequestStatus.PENDING);

            for (ParticipationRequest pending : pendingRequests) {
                pending.setStatus(RequestStatus.REJECTED);
                participationRequestRepository.save(pending);
                rejected.add(participationRequestMapper.mapToDto(pending));
            }
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }


}
