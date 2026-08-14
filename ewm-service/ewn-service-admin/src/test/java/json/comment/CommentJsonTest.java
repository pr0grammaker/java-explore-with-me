package json.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.comment.CommentDto;
import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class CommentJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testCommentDtoSerialization_AllFields() throws Exception {
        LocalDateTime createdOn = LocalDateTime.of(2026, 8, 10, 14, 30, 0);
        LocalDateTime updatedOn = LocalDateTime.of(2026, 8, 10, 15, 0, 0);

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Отличный концерт!")
                .author(new UserShortDto(2L, "Artem"))
                .eventId(10L)
                .createdOn(createdOn)
                .updatedOn(updatedOn)
                .build();

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Отличный концерт!");
        assertThat(result).extractingJsonPathNumberValue("$.author.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.author.name").isEqualTo("Artem");
        assertThat(result).extractingJsonPathNumberValue("$.eventId").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.createdOn").isEqualTo("2026-08-10 14:30:00");
        assertThat(result).extractingJsonPathStringValue("$.updatedOn").isEqualTo("2026-08-10 15:00:00");
    }

    @Test
    void testCommentDtoSerialization_NullUpdatedOnIgnored() throws Exception {
        LocalDateTime createdOn = LocalDateTime.of(2026, 8, 10, 14, 30, 0);

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Без обновления")
                .author(new UserShortDto(2L, "Artem"))
                .eventId(10L)
                .createdOn(createdOn)
                .updatedOn(null)
                .build();

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.text");
        assertThat(result).hasJsonPath("$.createdOn");
        assertThat(result).doesNotHaveJsonPath("$.updatedOn");
    }

    @Test
    void testCommentDtoDeserialization_FullJson() throws Exception {
        String jsonContent = "{\n" +
                "  \"id\": 100,\n" +
                "  \"text\": \"Десериализованный комментарий\",\n" +
                "  \"author\": {\n" +
                "    \"id\": 5,\n" +
                "    \"name\": \"Artem\"\n" +
                "  },\n" +
                "  \"eventId\": 42,\n" +
                "  \"createdOn\": \"2026-08-10 12:00:00\",\n" +
                "  \"updatedOn\": \"2026-08-10 13:00:00\"\n" +
                "}";

        CommentDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getText()).isEqualTo("Десериализованный комментарий");
        assertThat(dto.getAuthor()).isNotNull();
        assertThat(dto.getAuthor().getId()).isEqualTo(5L);
        assertThat(dto.getAuthor().getName()).isEqualTo("Artem");
        assertThat(dto.getEventId()).isEqualTo(42L);
        assertThat(dto.getCreatedOn()).isEqualTo(LocalDateTime.of(2026, 8, 10, 12, 0, 0));
        assertThat(dto.getUpdatedOn()).isEqualTo(LocalDateTime.of(2026, 8, 10, 13, 0, 0));
    }

    @Test
    void testCommentDtoDeserialization_WithoutUpdatedOn() throws Exception {
        String jsonContent = "{\n" +
                "  \"id\": 100,\n" +
                "  \"text\": \"Комментарий без обновления\",\n" +
                "  \"author\": {\n" +
                "    \"id\": 5,\n" +
                "    \"name\": \"Artem\"\n" +
                "  },\n" +
                "  \"eventId\": 42,\n" +
                "  \"createdOn\": \"2026-08-10 12:00:00\"\n" +
                "}";

        CommentDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getUpdatedOn()).isNull();
    }
}
