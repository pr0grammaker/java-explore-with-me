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
import ru.practicum.exceptions.ConditionsNotMetException;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentPrivateServiceImpl implements CommentPrivateService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;


    @Override
    public CommentDto addComment(Long userId, Long eventId, CommentCreateDto commentCreateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=%d не найдено".formatted(eventId)));

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new InvalidEventOperationException("Нельзя добавить комментарий к неопубликованному событию");
        }

        if (!event.getAllowComments()) {
            throw new ConditionsNotMetException("Нельзя добавить комментарий к событию у которого они отключены");
        }

        Comment comment = Comment.builder()
                .text(commentCreateDto.getText())
                .event(event)
                .author(user)
                .createdOn(LocalDateTime.now())
                .updatedOn(null)
                .build();

        Comment savedComment = commentRepository.save(comment);
        eventRepository.incrementCommentsCount(eventId);

        return commentMapper.mapToCommentDto(savedComment);
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, CommentUpdateDto commentUpdateDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId)));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий пользователя с id=%d не найден".formatted(commentId)));

        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConditionsNotMetException("Пользователь с id=%d не является автором комментария".formatted(userId));
        }

        if (commentUpdateDto.getText() != null && !commentUpdateDto.getText().equals(comment.getText())) {
            comment.setText(commentUpdateDto.getText());
            comment.setUpdatedOn(LocalDateTime.now());
        }

        Comment updated = commentRepository.save(comment);
        return commentMapper.mapToCommentDto(updated);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId)));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий пользователя с id=%d не найден".formatted(commentId)));

        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConditionsNotMetException("Пользователь с id=%d не является автором комментария".formatted(userId));
        }

        commentRepository.deleteById(commentId);
        eventRepository.decrementCommentsCount(comment.getEvent().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CommentDto> getUserComments(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId)));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "createdOn"));

        List<Comment> comments = commentRepository.findAllByAuthorId(userId, pageable);

        return comments.stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
    }
}
