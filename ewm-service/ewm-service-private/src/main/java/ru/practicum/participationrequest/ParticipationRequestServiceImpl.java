package ru.practicum.participationrequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.event.RequestStatus;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper participationRequestMapper;
    private final EventRepository eventRepository;

    @Override
    public Collection<ParticipationRequestDto> getParticipationRequestsByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = %d не найден".formatted(userId)));

        List<ParticipationRequest> requests =
                participationRequestRepository.findUserRequestsForOtherEvents(userId);

        return requests.stream()
                .map(participationRequestMapper::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = %d не найден".formatted(userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = %d не найдено".formatted(eventId)));

        if (participationRequestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new InvalidEventOperationException("Заявка уже существует");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new InvalidEventOperationException("Инициатор не может подать заявку на своё событие");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new InvalidEventOperationException("Нельзя участвовать в неопубликованном событии");
        }

        long confirmedCount = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new InvalidEventOperationException("Лимит заявок на событие достигнут");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(event.getParticipantLimit() == 0 || !event.getRequestModeration()
                        ? RequestStatus.CONFIRMED
                        : RequestStatus.PENDING)
                .build();

        participationRequestRepository.save(request);
        return participationRequestMapper.mapToDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto updateParticipationRequest(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId)));

        ParticipationRequest request = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос на участие с id = %d не был найден"
                        .formatted(requestId)));

        if (!request.getRequester().getId().equals(userId)) {
            throw new InvalidEventOperationException("Заявка id=%d не принадлежит пользователю id=%d"
                    .formatted(requestId, userId));
        }

        request.setStatus(RequestStatus.CANCELED);
        participationRequestRepository.save(request);

        return participationRequestMapper.mapToDto(request);
    }

}
