package ru.practicum.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    EventFullDto mapToEventFullDto(Event event);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    EventFullDto mapToEventFullDto(
            Event event,
            long confirmedRequests,
            Long views
    );

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    EventCompilationDto mapToEventCompilationDto(
            Event event,
            Long confirmedRequests,
            Long views
    );
}
