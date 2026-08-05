package json.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.category.CategoryDto;
import ru.practicum.event.EventFullDto;
import ru.practicum.event.EventState;
import ru.practicum.event.Location;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class EventJsonTest {

    @Autowired
    private JacksonTester<EventFullDto> json;

    @Test
    void testSerializeEventFullDto() throws Exception {
        CategoryDto category = CategoryDto.builder()
                .id(10L)
                .name("Концерт")
                .build();

        UserShortDto initiator = UserShortDto.builder()
                .id(5L)
                .name("Иван Иванов")
                .build();

        Location location = Location.builder()
                .lat(51.1694f)
                .lon(71.4491f)
                .build();

        EventFullDto dto = EventFullDto.builder()
                .id(1L)
                .title("Новое событие")
                .annotation("Аннотация")
                .description("Описание")
                .category(category)
                .initiator(initiator)
                .location(location)
                .eventDate(LocalDateTime.of(2024, 12, 31, 15, 10, 5))
                .createdOn(LocalDateTime.of(2024, 12, 1, 12, 0))
                .publishedOn(LocalDateTime.of(2024, 12, 2, 12, 0))
                .confirmedRequests(5L)
                .views(100L)
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .build();

        assertThat(json.write(dto)).isEqualToJson(
                "{\"id\":1,\"title\":\"Новое событие\",\"annotation\":\"Аннотация\",\"description\":\"Описание\",\"category\":{\"id\":10,\"name\":\"Концерт\"},\"initiator\":{\"id\":5,\"name\":\"Иван Иванов\"},\"location\":{\"lat\":51.1694,\"lon\":71.4491},\"eventDate\":\"2024-12-31T15:10:05\",\"createdOn\":\"2024-12-01T12:00:00\",\"publishedOn\":\"2024-12-02T12:00:00\",\"confirmedRequests\":5,\"views\":100,\"paid\":true,\"participantLimit\":10,\"requestModeration\":true,\"state\":\"PUBLISHED\"}"
        );
    }

    @Test
    void testDeserializeEventFullDto() throws Exception {
        String content = "{\"id\":1,\"title\":\"Новое событие\",\"annotation\":\"Аннотация\",\"description\":\"Описание\",\"category\":{\"id\":10,\"name\":\"Концерт\"},\"initiator\":{\"id\":5,\"name\":\"Иван Иванов\"},\"location\":{\"lat\":51.1694,\"lon\":71.4491},\"eventDate\":\"2024-12-31T15:10:05\",\"createdOn\":\"2024-12-01T12:00:00\",\"publishedOn\":\"2024-12-02T12:00:00\",\"confirmedRequests\":5,\"views\":100,\"paid\":true,\"participantLimit\":10,\"requestModeration\":true,\"state\":\"PUBLISHED\"}";

        EventFullDto parsed = json.parseObject(content);

        assertThat(parsed.getId()).isEqualTo(1L);
        assertThat(parsed.getTitle()).isEqualTo("Новое событие");
        assertThat(parsed.getCategory().getName()).isEqualTo("Концерт");
        assertThat(parsed.getInitiator().getName()).isEqualTo("Иван Иванов");
        assertThat(parsed.getState()).isEqualTo(EventState.PUBLISHED);
    }
}
