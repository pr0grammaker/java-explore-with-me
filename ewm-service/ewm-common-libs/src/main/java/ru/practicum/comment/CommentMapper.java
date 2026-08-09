package ru.practicum.comment;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface CommentMapper {

    CommentDto mapToCommentDto(Comment comment);
}
