package mock.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.AdminAppExploreWithMe;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.NewUserRequest;
import ru.practicum.user.UserController;
import ru.practicum.user.UserDto;
import ru.practicum.user.UserService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = AdminAppExploreWithMe.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;

    private NewUserRequest newUserRequest;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(
                "john.doe@example.com",
                1L,
                "John Doe");

        newUserRequest = new NewUserRequest("john.doe@example.com", "John Doe");
    }

    @Test
    void findAll() throws Exception {
        when(userService.findAll(null, 0, 10))
                .thenReturn(List.of(userDto, userDto, userDto, userDto, userDto));

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void findAll_OthersFromAndSize() throws Exception {
        when(userService.findAll(null, 1, 3))
                .thenReturn(List.of(userDto, userDto, userDto));

        mockMvc.perform(get("/admin/users")
                        .param("from", "1")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void findAll_FilterByIds() throws Exception {
        when(userService.findAll(List.of(1L), 0, 10))
                .thenReturn(List.of(userDto));

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void findAll_EmptyResult() throws Exception {
        when(userService.findAll(null, 0, 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void findAll_SizeZero() throws Exception {
        when(userService.findAll(null, 0, 0))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void findAll_SortedByIdAsc() throws Exception {
        UserDto userDto2 = new UserDto("mark.doe@example.com", 2L, "Mark Twen");

        when(userService.findAll(null, 0, 10))
                .thenReturn(List.of(userDto, userDto2));

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void addUser() throws Exception {
        when(userService.addUser(newUserRequest))
                .thenReturn(userDto);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void addUser_invalidEmailFormat() throws Exception {
        NewUserRequest invalid = NewUserRequest.builder()
                .email("invalid-email")
                .name("John Doe")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Некорректный формат email"));
    }

    @Test
    void addUser_blankEmail() throws Exception {
        NewUserRequest invalid = NewUserRequest.builder()
                .email(" ")
                .name("John Doe")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_shortEmail() throws Exception {
        NewUserRequest invalid = NewUserRequest.builder()
                .email("a@b.c")
                .name("John Doe")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email должен быть от 6 до 254 символов"));
    }

    @Test
    void addUser_blankName() throws Exception {
        NewUserRequest invalid = NewUserRequest.builder()
                .email("john.doe@example.com")
                .name(" ")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_shortName() throws Exception {
        NewUserRequest invalid = NewUserRequest.builder()
                .email("john.doe@example.com")
                .name("J")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Имя должно быть от 2 до 250 символов"));
    }


    @Test
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/admin/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_NotFound() throws Exception {
        doThrow(new NotFoundException("Пользователь не найден"))
                .when(userService).delete(999L);

        mockMvc.perform(delete("/admin/users/{userId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь не найден"));
    }
}
