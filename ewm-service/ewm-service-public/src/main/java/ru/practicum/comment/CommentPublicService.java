package ru.practicum.comment;

import java.util.Collection;

public interface CommentPublicService {

    Collection<CommentDto> getEventComments(Long eventId, int from, int size, String sort);
}
