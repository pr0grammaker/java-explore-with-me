package ru.practicum.hits;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.EndpointHitDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EndpointMapper {

    EndpointHit mapToEndpointHit(EndpointHitDto endpointHitDto);


    EndpointHitDto mapToEndpointHitDto(EndpointHit entity);
}
