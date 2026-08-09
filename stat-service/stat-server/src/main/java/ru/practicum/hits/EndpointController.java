package ru.practicum.hits;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Transactional(readOnly = true)
public class EndpointController {

    private final EndpointHitService endpointHitService;

    @PostMapping("/hit")
    @Transactional
    public ResponseEntity<EndpointHitDto> saveHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(endpointHitService.save(endpointHitDto));
    }

    @GetMapping("/stats")
    public ResponseEntity<Collection<ViewStats>> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(endpointHitService.get(start, end, uris, unique));
    }
}
