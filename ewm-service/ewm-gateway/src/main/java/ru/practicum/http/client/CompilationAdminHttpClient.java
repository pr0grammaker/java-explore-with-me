package ru.practicum.http.client;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import ru.practicum.compilation.CompilationDto;
import ru.practicum.compilation.NewCompilationDto;
import ru.practicum.compilation.UpdateCompilationRequest;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/admin/compilations"
)
public interface CompilationAdminHttpClient {

    @PostExchange
    ResponseEntity<CompilationDto> addCompilation(
            @RequestBody @Valid NewCompilationDto newCompilationDto
    );

    @DeleteExchange("/{compId}")
    void deleteCompilation(
            @PathVariable("compId") Long compId
    );

    @PatchExchange("/{compId}")
    ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable("compId") Long compId,
            @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest
    );
}
