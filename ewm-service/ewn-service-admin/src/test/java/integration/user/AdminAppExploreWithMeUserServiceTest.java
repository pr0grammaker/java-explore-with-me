package integration.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.exceptions.DuplicatedDataException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.NewUserRequest;
import ru.practicum.user.UserDto;
import ru.practicum.user.UserRepository;
import ru.practicum.user.UserService;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = AdminAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class AdminAppExploreWithMeUserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    private UserDto createUser(String email, String name) {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .email(email)
                .name(name)
                .build();

        return userService.addUser(newUserRequest);
    }

    @Test
    void testAddUser_Success() {
        UserDto userDto = createUser("john.doe@example.com", "John Doe");

        assertThat(userDto)
                .isNotNull()
                .extracting(UserDto::getEmail, UserDto::getName)
                .containsExactly("john.doe@example.com", "John Doe");
    }

    @Test
    void testAddUser_existsByEmail() {
        createUser("john.doe@example.com", "John Doe");

        assertThatThrownBy(() -> createUser("john.doe@example.com", "John Doe"))
                .isInstanceOf(DuplicatedDataException.class)
                .hasMessageContaining("Пользователь с таким email уже существует");
    }

    @Test
    void deleteUser_Success() {
        UserDto userDto = createUser("john.doe@example.com", "John Doe");

        userService.delete(userDto.getId());

        assertThat(userRepository.findById(userDto.getId())).isEmpty();
    }

    @Test
    void deleteUser_throwNotFoundException() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void findAll_returnAllUsers() {
        UserDto userDto1 = createUser("john.doe@example.com", "John Doe");
        UserDto userDto2 = createUser("mark.doe@example.com", "Mark Twen");

        Collection<UserDto> result = userService.findAll(null, 0, 10);

        assertThat(result).hasSize(2)
                .extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder(userDto1.getEmail(), userDto2.getEmail());
    }

    @Test
    void findAll_filterByIds() {
        UserDto userDto1 = createUser("john.doe@example.com", "John Doe");
        createUser("mark.doe@example.com", "Mark Twen");

        Collection<UserDto> result = userService.findAll(List.of(userDto1.getId()), 0, 10);

        assertThat(result).hasSize(1)
                .first()
                .extracting(UserDto::getEmail)
                .isEqualTo(userDto1.getEmail());
    }

    @Test
    void findAll_paginationWorks() {
        createUser("john.doe@example.com", "John Doe");
        createUser("mark.doe@example.com", "Mark Twen");
        UserDto userDto3 = createUser("bob.smith@example.com", "Bob Smith");

        Collection<UserDto> result = userService.findAll(null, 2, 1);

        assertThat(result).hasSize(1)
                .first()
                .extracting(UserDto::getEmail)
                .isEqualTo(userDto3.getEmail());
    }

    @Test
    void findAll_emptyResult() {
        Collection<UserDto> result = userService.findAll(List.of(999L), 0, 10);
        assertThat(result).isEmpty();
    }

}
