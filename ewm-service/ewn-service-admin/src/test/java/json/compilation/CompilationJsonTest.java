package json.compilation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.category.CategoryDto;
import ru.practicum.event.EventCompilationDto;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class CompilationJsonTest {
    @Autowired
    private JacksonTester<EventCompilationDto> json;

    @Test
    void testSerializeEventCompilationDto() throws Exception {
        EventCompilationDto dto = EventCompilationDto.builder()
                .id(1L)
                .title("Новое событие")
                .annotation("Аннотация")
                .category(CategoryDto.builder().id(10L).name("Концерт").build())
                .initiator(UserShortDto.builder().id(5L).name("Иван Иванов").build())
                .eventDate(LocalDateTime.of(2024, 12, 31, 15, 10, 5))
                .confirmedRequests(5L)
                .views(100L)
                .paid(true)
                .build();

        assertThat(json.write(dto)).isEqualToJson(
                "{\"id\":1,\"title\":\"Новое событие\",\"annotation\":\"Аннотация\",\"category\":{\"id\":10,\"name\":\"Концерт\"},\"initiator\":{\"id\":5,\"name\":\"Иван Иванов\"},\"eventDate\":\"2024-12-31T15:10:05\",\"confirmedRequests\":5,\"views\":100,\"paid\":true}"
        );
    }

    @Test
    void testDeserializeEventCompilationDto() throws Exception {
        String content = "{\"id\":1,\"title\":\"Новое событие\",\"annotation\":\"Аннотация\",\"category\":{\"id\":10,\"name\":\"Концерт\"},\"initiator\":{\"id\":5,\"name\":\"Иван Иванов\"},\"eventDate\":\"2024-12-31T15:10:05\",\"confirmedRequests\":5,\"views\":100,\"paid\":true}";

        EventCompilationDto parsed = json.parseObject(content);

        assertThat(parsed.getId()).isEqualTo(1L);
        assertThat(parsed.getTitle()).isEqualTo("Новое событие");
        assertThat(parsed.getCategory().getName()).isEqualTo("Концерт");
        assertThat(parsed.getInitiator().getName()).isEqualTo("Иван Иванов");
        assertThat(parsed.getConfirmedRequests()).isEqualTo(5L);
        assertThat(parsed.getViews()).isEqualTo(100L);
        assertThat(parsed.getPaid()).isTrue();
    }

}
