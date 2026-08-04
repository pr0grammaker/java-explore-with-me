package ru.practicum.event;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    EventFullDto mapToEventFullDto(Event event);

    EventFullDto mapToEventFullDto(Event e, long confirmedRequests, Long views);

    EventCompilationDto mapToEventCompilationDto(Event event,
                                                 Long confirmedRequests,
                                                 Long views);
}
