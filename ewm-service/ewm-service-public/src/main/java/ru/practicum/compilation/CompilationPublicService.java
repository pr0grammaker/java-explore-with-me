package ru.practicum.compilation;

import java.util.Collection;

public interface CompilationPublicService {

    Collection<CompilationDto> getAllCompilations(boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long compId);
}
