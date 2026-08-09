package ru.practicum.comment;

import java.util.Collection;

public interface CommentPrivateService {
    CommentDto addComment(Long userId, Long eventId, CommentCreateDto commentCreateDto);

    CommentDto updateComment(Long userId, Long commentId, CommentUpdateDto commentUpdateDto);

    void deleteComment(Long userId, Long commentId);

    Collection<CommentDto> getUserComments(Long userId, int from, int size);
}
