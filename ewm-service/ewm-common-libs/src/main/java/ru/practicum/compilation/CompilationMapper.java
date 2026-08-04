package ru.practicum.compilation;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompilationMapper {

    CompilationDto mapToCompilationDto(Compilation compilation);
}
