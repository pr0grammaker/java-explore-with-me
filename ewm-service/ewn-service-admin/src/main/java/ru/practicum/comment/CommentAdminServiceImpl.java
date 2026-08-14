package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.EventRepository;
import ru.practicum.exceptions.NotFoundException;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentAdminServiceImpl implements CommentAdminService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;


    @Override
    public Collection<CommentDto> searchComments(List<Long> users, List<Long> events, String text, int from, int size) {
        int page = (size > 0) ? from / size : 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn"));

        List<Long> userIds = (users != null && !users.isEmpty()) ? users : null;
        List<Long> eventIds = (events != null && !events.isEmpty()) ? events : null;
        String searchText = (text != null && !text.isBlank()) ? text : null;

        List<Comment> comments = commentRepository.searchCommentsByAdmin(userIds, eventIds, searchText, pageable);

        return comments.stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий пользователя с id=%d не найден".formatted(commentId)));

        commentRepository.deleteById(commentId);
        eventRepository.decrementCommentsCount(comment.getEvent().getId());
    }
}
