package ru.practicum.http.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import ru.practicum.compilation.CompilationDto;

import java.util.Collection;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/compilations"
)
public interface CompilationPublicHttpClient {

    @GetExchange
    ResponseEntity<Collection<CompilationDto>> getAllCompilations(
            @RequestParam(defaultValue = "false", required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetExchange("/{compId}")
    ResponseEntity<CompilationDto> getCompilationById(
            @PathVariable("compId") Long compId
    );
}
