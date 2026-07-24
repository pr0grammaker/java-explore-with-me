package ru.practicum.hits;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class EndpointController {

    private final EndpointHitService endpointHitService;

    @PostMapping("/hit")
    public ResponseEntity<EndpointHitDto> saveHit(@RequestBody EndpointHitDto endpointHitDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(endpointHitService.save(endpointHitDto));
    }

    @GetMapping("/stats")
    public ResponseEntity<Collection<ViewStats>> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(endpointHitService.get(start, end, uris, unique));
    }
}
