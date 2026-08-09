package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.http.client.CompilationPublicHttpClient;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationPublicController {

    private final CompilationPublicHttpClient compilationPublicHttpClient;

    @GetMapping
    public ResponseEntity<Collection<CompilationDto>> getAllCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(compilationPublicHttpClient.getAllCompilations(pinned, from, size).getBody());
    }

    @GetMapping("{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(
            @PathVariable("compId") Long compId
    ) {
        return ResponseEntity.ok()
                .body(compilationPublicHttpClient.getCompilationById(compId).getBody());
    }
}
