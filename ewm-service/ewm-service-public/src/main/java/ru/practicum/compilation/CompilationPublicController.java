package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationPublicController {

    private final CompilationPublicService compilationPublicService;

    @GetMapping
    public ResponseEntity<Collection<CompilationDto>> getAllCompilations(
            @RequestParam boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(compilationPublicService.getAllCompilations(pinned, from, size));
    }

    @GetMapping("{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(
            @PathVariable("compId") Long compId
    ) {
        return ResponseEntity.ok().body(compilationPublicService.getCompilationById(compId));
    }
}
