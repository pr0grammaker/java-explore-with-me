package json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.StatApplicationServer;
import ru.practicum.dto.EndpointHitDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = StatApplicationServer.class)
public class EndpointHitDtoJsonTest {

    @Autowired
    private JacksonTester<EndpointHitDto> json;

    @Test
    void serializeEndpointHitDtoTest() throws IOException {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 26, 12, 30, 0);

        EndpointHitDto dto = EndpointHitDto.builder()
                .app("stats-service")
                .uri("/items/42")
                .ip("192.168.0.1")
                .timestamp(timestamp)
                .build();

        JsonContent<EndpointHitDto> result = json.write(dto);

        String expectedJson = """
                {"app":"stats-service","uri":"/items/42","ip":"192.168.0.1","timestamp":"2026-07-26 12:30:00"}""";

        assertThat(result).isEqualToJson(expectedJson);
    }

    @Test
    void deserializeEndpointHitDtoTest() throws Exception {
        String content = """
                {"app":"stats-service","uri":"/items/42","ip":"192.168.0.1","timestamp":"2026-07-26 12:30:00"}""";


        EndpointHitDto parsed = json.parseObject(content);

        assertThat(parsed.getApp()).isEqualTo("stats-service");
        assertThat(parsed.getUri()).isEqualTo("/items/42");
        assertThat(parsed.getIp()).isEqualTo("192.168.0.1");
        assertThat(parsed.getTimestamp()).isEqualTo(LocalDateTime.of(2026, 7, 26, 12, 30, 0));
    }
}
