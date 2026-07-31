package ru.practicum.participationrequest;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ParticipationRequestMapper {

    ParticipationRequestDto mapToDto(ParticipationRequest participationRequest);

}
