package ru.practicum.compilation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.http.client.CompilationAdminHttpClient;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class CompilationAdminController {

    private final CompilationAdminHttpClient compilationAdminHttpClient;

    @PostMapping
    public ResponseEntity<CompilationDto> addCompilation(
            @RequestBody @Valid NewCompilationDto newCompilationDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compilationAdminHttpClient.addCompilation(newCompilationDto).getBody());
    }

    @DeleteMapping("{compId}")
    public ResponseEntity<Void> deleteCompilation(
            @PathVariable("compId") Long compId
    ) {
        compilationAdminHttpClient.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable("compId") Long compId,
            @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest
    ) {
        return ResponseEntity.ok()
                .body(compilationAdminHttpClient.updateCompilation(compId, updateCompilationRequest).getBody());
    }
}
