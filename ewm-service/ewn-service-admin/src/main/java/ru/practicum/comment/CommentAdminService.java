package ru.practicum.comment;

import java.util.Collection;
import java.util.List;

public interface CommentAdminService {

    Collection<CommentDto> searchComments(List<Long> users, List<Long> events, String text, int from, int size);

    void deleteCommentByAdmin(Long commentId);
}
