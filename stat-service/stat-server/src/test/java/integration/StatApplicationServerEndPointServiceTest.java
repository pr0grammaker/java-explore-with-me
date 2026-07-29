package integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.StatApplicationServer;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.hits.EndpointHitRepository;
import ru.practicum.hits.EndpointHitService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = StatApplicationServer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class StatApplicationServerEndPointServiceTest {

    @Autowired
    private EndpointHitService endpointHitService;

    @Autowired
    private EndpointHitRepository endpointHitRepository;

    @BeforeEach
    public void setup() {
        endpointHitRepository.deleteAll();
    }

    private EndpointHitDto createEndpointHitDto(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();

        return endpointHitService.save(endpointHitDto);
    }

    @Test
    void saveHitTest_Success() {
        LocalDateTime timestamp = LocalDateTime.now();

        EndpointHitDto endpointHitDto = createEndpointHitDto(
                "stats-service",
                "/items/42", "192.168.0.1",
                timestamp);

        assertThat(endpointHitDto)
                .isNotNull()
                .extracting(EndpointHitDto::getApp,
                        EndpointHitDto::getUri,
                        EndpointHitDto::getIp,
                        EndpointHitDto::getTimestamp)
                .containsExactly("stats-service", "/items/42", "192.168.0.1", timestamp);
    }

    @Test
    void saveHit_Success_WithLowBoundaryIp() {
        LocalDateTime timestamp = LocalDateTime.now();
        EndpointHitDto dto = new EndpointHitDto("stats-service", "/items/42", "0.0.0.0", timestamp);

        EndpointHitDto saved = endpointHitService.save(dto);

        assertThat(saved.getIp()).isEqualTo("0.0.0.0");
    }

    @Test
    void saveHit_Success_WithBoundaryIp() {
        LocalDateTime timestamp = LocalDateTime.now();
        EndpointHitDto dto = new EndpointHitDto("stats-service", "/items/42", "255.255.255.255", timestamp);

        EndpointHitDto saved = endpointHitService.save(dto);

        assertThat(saved.getIp()).isEqualTo("255.255.255.255");
    }

    @Test
    void getStats_EmptyResult() {
        Collection<ViewStats> stats = endpointHitService.get(
                LocalDateTime.of(2026, 7, 20, 0, 0, 0),
                LocalDateTime.of(2026, 7, 21, 0, 0, 0),
                List.of("/items/42"),
                false
        );

        assertThat(stats).isEmpty();
    }

    @Test
    void getStats_Success_WithUri() {
        LocalDateTime ts1 = LocalDateTime.of(2026, 7, 25, 12, 0, 0);
        LocalDateTime ts2 = LocalDateTime.of(2026, 7, 25, 13, 0, 0);

        createEndpointHitDto("stats-service", "/items/42", "192.168.0.1", ts1);
        createEndpointHitDto("stats-service", "/items/42", "192.168.0.2", ts2);

        Collection<ViewStats> stats = endpointHitService.get(
                LocalDateTime.of(2026, 7, 25, 0, 0, 0),
                LocalDateTime.of(2026, 7, 26, 0, 0, 0),
                List.of("/items/42"),
                false
        );

        assertThat(stats).hasSize(1);
        ViewStats viewStats = stats.iterator().next();
        assertThat(viewStats.getApp()).isEqualTo("stats-service");
        assertThat(viewStats.getUri()).isEqualTo("/items/42");
        assertThat(viewStats.getHits()).isEqualTo(2L);
    }

    @Test
    void getStats_Success_WithoutUris() {
        LocalDateTime ts = LocalDateTime.of(2026, 7, 25, 12, 0, 0);

        createEndpointHitDto("stats-service", "/items/42", "192.168.0.1", ts);
        createEndpointHitDto("stats-service", "/items/99", "192.168.0.2", ts);

        Collection<ViewStats> stats = endpointHitService.get(
                LocalDateTime.of(2026, 7, 25, 0, 0, 0),
                LocalDateTime.of(2026, 7, 26, 0, 0, 0),
                null,
                false
        );

        assertThat(stats).hasSize(2);
        assertThat(stats).extracting(ViewStats::getUri)
                .containsExactlyInAnyOrder("/items/42", "/items/99");
    }

    @Test
    void getStats_Success_UniqueTrue() {
        LocalDateTime ts = LocalDateTime.of(2026, 7, 25, 12, 0, 0);

        createEndpointHitDto("stats-service", "/items/42", "192.168.0.1", ts);
        createEndpointHitDto("stats-service", "/items/42", "192.168.0.1", ts.plusMinutes(5));

        Collection<ViewStats> stats = endpointHitService.get(
                LocalDateTime.of(2026, 7, 25, 0, 0, 0),
                LocalDateTime.of(2026, 7, 26, 0, 0, 0),
                List.of("/items/42"),
                true
        );

        assertThat(stats).hasSize(1);
        ViewStats viewStats = stats.iterator().next();
        assertThat(viewStats.getHits()).isEqualTo(1L); // уникальные IP
    }

}
