package json.user;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.user.NewUserRequest;
import ru.practicum.user.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class UserJsonTest {
    @Autowired
    private JacksonTester<UserDto> userJson;

    @Autowired
    private JacksonTester<NewUserRequest> newUserJson;

    @Test
    void testSerializeUserDto() throws Exception {
        UserDto dto = new UserDto("john.doe@example.com", 1L, "John Doe");

        assertThat(userJson.write(dto)).isEqualToJson("""
            {
              "email": "john.doe@example.com",
              "id": 1,
              "name": "John Doe"
            }
        """);
    }

    @Test
    void testDeserializeUserDto() throws Exception {
        String content = """
            {
              "email": "john.doe@example.com",
              "id": 1,
              "name": "John Doe"
            }
        """;

        UserDto parsed = userJson.parseObject(content);

        assertThat(parsed.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(parsed.getId()).isEqualTo(1L);
        assertThat(parsed.getName()).isEqualTo("John Doe");
    }

    @Test
    void testSerializeNewUserRequest() throws Exception {
        NewUserRequest req = new NewUserRequest();
        req.setEmail("john.doe@example.com");
        req.setName("John Doe");

        assertThat(newUserJson.write(req)).isEqualToJson("""
            {
              "email": "john.doe@example.com",
              "name": "John Doe"
            }
        """);
    }

    @Test
    void testDeserializeNewUserRequest() throws Exception {
        String content = """
            {
              "email": "john.doe@example.com",
              "name": "John Doe"
            }
        """;

        NewUserRequest parsed = newUserJson.parseObject(content);

        assertThat(parsed.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(parsed.getName()).isEqualTo("John Doe");
    }


}
