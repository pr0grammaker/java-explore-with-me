package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventState;
import ru.practicum.exceptions.NotFoundException;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentPublicServiceImpl implements CommentPublicService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;


    @Override
    public Collection<CommentDto> getEventComments(Long eventId, int from, int size, String sort) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=%d не найдено".formatted(eventId)));

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException("Событие с id=%d не опубликовано".formatted(eventId));
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sort)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        int page = (size > 0) ? from / size : 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdOn"));

        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageable);

        return comments.stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
    }
}
