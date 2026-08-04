package ru.practicum.compilation;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.EventCompilationDto;
import ru.practicum.event.EventMapper;
import ru.practicum.event.RequestStatus;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.participationrequest.ParticipationRequestRepository;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationPublicServiceImpl implements CompilationPublicService {

    private final CompilationRepository compilationRepository;
    private final EventMapper eventMapper;
    private final ParticipationRequestRepository participationRequestRepository;
    private final CompilationMapper compilationMapper;


    @Override
    public Collection<CompilationDto> getAllCompilations(boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);

        return compilations.stream()
                .map(compilation -> {
                    List<EventCompilationDto> eventDtos = compilation.getEvents().stream()
                            .map(e -> eventMapper.mapToEventCompilationDto(
                                    e,
                                    participationRequestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED),
                                    e.getViews()))
                            .toList();

                    return CompilationDto.builder()
                            .id(compilation.getId())
                            .title(compilation.getTitle())
                            .pinned(compilation.getPinned())
                            .events(eventDtos)
                            .build();
                })
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=%d не найдена".formatted(compId)));

        return compilationMapper.mapToCompilationDto(compilation);
    }
}
