package json.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.category.CategoryDto;
import ru.practicum.category.NewCategoryDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class CategoryJsonTest {
    @Autowired
    private JacksonTester<NewCategoryDto> newCategoryJson;

    @Autowired
    private JacksonTester<CategoryDto> categoryJson;

    @Test
    void testSerializeNewCategoryDto() throws Exception {
        NewCategoryDto dto = NewCategoryDto.builder()
                .name("Концерты")
                .build();

        assertThat(newCategoryJson.write(dto))
                .isEqualToJson("{\"name\":\"Концерты\"}");
    }

    @Test
    void testDeserializeNewCategoryDto() throws Exception {
        String content = "{\"name\":\"Фестивали\"}";

        NewCategoryDto parsed = newCategoryJson.parseObject(content);

        assertThat(parsed.getName()).isEqualTo("Фестивали");
    }

    @Test
    void testSerializeCategoryDto() throws Exception {
        CategoryDto dto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();

        assertThat(categoryJson.write(dto))
                .isEqualToJson("{\"id\":1,\"name\":\"Концерты\"}");
    }

    @Test
    void testDeserializeCategoryDto() throws Exception {
        String content = "{\"id\":2,\"name\":\"Фестивали\"}";

        CategoryDto parsed = categoryJson.parseObject(content);

        assertThat(parsed.getId()).isEqualTo(2L);
        assertThat(parsed.getName()).isEqualTo("Фестивали");
    }

}
