package ru.practicum.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface CommentMapper {
    @Mapping(target = "eventId", source = "event.id")
    CommentDto mapToCommentDto(Comment comment);
}
