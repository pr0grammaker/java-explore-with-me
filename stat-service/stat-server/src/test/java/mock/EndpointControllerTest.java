package mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.StatApplicationServer;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.hits.EndpointController;
import ru.practicum.hits.EndpointHitService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EndpointController.class)
@ContextConfiguration(classes = StatApplicationServer.class)
public class EndpointControllerTest {

    @MockBean
    private EndpointHitService endpointHitService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private EndpointHitDto endpointHitDto;

    private final LocalDateTime timestamp = LocalDateTime.now();

    @BeforeEach
    public void setup() {
        endpointHitDto = new EndpointHitDto(
                "stats-service",
                "/items/42", "192.168.0.1",
                timestamp
        );

    }

    @Test
    void saveHitTest_Success() throws Exception {
        when(endpointHitService.save(any(EndpointHitDto.class))).thenReturn(endpointHitDto);

        mockMvc.perform(post("/hit")
                        .content(objectMapper.writeValueAsString(endpointHitDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value(endpointHitDto.getApp()))
                .andExpect(jsonPath("$.uri").value(endpointHitDto.getUri()))
                .andExpect(jsonPath("$.timestamp").value(endpointHitDto.getTimestamp()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));


        verify(endpointHitService, times(1)).save(any(EndpointHitDto.class));
    }

    @Test
    void saveHit_Fail_WhenAppIsBlank() throws Exception {
        endpointHitDto.setApp("");

        mockMvc.perform(post("/hit")
                        .content(objectMapper.writeValueAsString(endpointHitDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Название сервиса не должно быть пустым"));
    }

    @Test
    void saveHit_Fail_WhenUriInvalid() throws Exception {
        endpointHitDto.setUri("items/42"); // uri должен начинатся с "/"

        mockMvc.perform(post("/hit")
                        .content(objectMapper.writeValueAsString(endpointHitDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("URI должен начинаться с '/' и содержать допустимые символы"));
    }

    @Test
    void saveHit_Fail_WhenIpInvalid() throws Exception {
        endpointHitDto.setIp("999.999.999.999");

        mockMvc.perform(post("/hit")
                        .content(objectMapper.writeValueAsString(endpointHitDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("IP-адрес должен быть в формате IPv4"));
    }

    @Test
    void saveHit_ReturnsCorrectDto() throws Exception {
        when(endpointHitService.save(any(EndpointHitDto.class))).thenReturn(endpointHitDto);

        mockMvc.perform(post("/hit")
                        .content(objectMapper.writeValueAsString(endpointHitDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value("stats-service"))
                .andExpect(jsonPath("$.uri").value("/items/42"))
                .andExpect(jsonPath("$.ip").value("192.168.0.1"));
    }

    @Test
    void getStats_Success() throws Exception {
        List<ViewStats> stats = List.of(new ViewStats("stats-service", "/items/42", 5L));
        when(endpointHitService.get(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(stats);

        mockMvc.perform(get("/stats")
                        .param("start", "2026-07-25 00:00:00")
                        .param("end", "2026-07-26 00:00:00")
                        .param("uris", "/items/42")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("stats-service"))
                .andExpect(jsonPath("$[0].uri").value("/items/42"))
                .andExpect(jsonPath("$[0].hits").value(5));
    }

    @Test
    void getStats_WithoutUris() throws Exception {
        List<ViewStats> stats = List.of(new ViewStats("stats-service", "/items/42", 10L));
        when(endpointHitService.get(any(LocalDateTime.class), any(LocalDateTime.class), isNull(), anyBoolean()))
                .thenReturn(stats);

        mockMvc.perform(get("/stats")
                        .param("start", "2026-07-25 00:00:00")
                        .param("end", "2026-07-26 00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hits").value(10));
    }

    @Test
    void getStats_UniqueTrue() throws Exception {
        List<ViewStats> stats = List.of(new ViewStats("stats-service", "/items/42", 3L));
        when(endpointHitService.get(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(stats);

        mockMvc.perform(get("/stats")
                        .param("start", "2026-07-25 00:00:00")
                        .param("end", "2026-07-26 00:00:00")
                        .param("uris", "/items/42")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hits").value(3));
    }


    @Test
    void getStats_Fail_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2026-07-26") // неправильный формат: нет времени
                        .param("end", "2026-07-26 00:00:00"))
                .andExpect(status().isBadRequest());
    }


}
