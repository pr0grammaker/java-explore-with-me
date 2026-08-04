package ru.practicum.compilation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class CompilationAdminController {

    private final CompilationAdminService compilationAdminService;

    @PostMapping
    public ResponseEntity<CompilationDto> addCompilation(
            @RequestBody @Valid NewCompilationDto newCompilationDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compilationAdminService.addCompilation(newCompilationDto));
    }

    @DeleteMapping("{compId}")
    public ResponseEntity<Void> deleteCompilation(
            @PathVariable("compId") Long compId
    ) {
        compilationAdminService.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable("compId") Long compId,
            @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest
    ) {
        return ResponseEntity.ok()
                .body(compilationAdminService.updateCompilation(compId, updateCompilationRequest));
    }
}
