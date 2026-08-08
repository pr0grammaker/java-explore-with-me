package ru.practicum.http.client;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import ru.practicum.user.NewUserRequest;
import ru.practicum.user.UserDto;

import java.util.Collection;
import java.util.List;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/admin/users"
)
public interface UserPrivateHttpClient {

    @GetExchange
    ResponseEntity<Collection<UserDto>> findAll(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @PostExchange
    ResponseEntity<UserDto> add(@Valid @RequestBody NewUserRequest newUserRequest);

    @DeleteExchange("/{userId}")
    void delete(@PathVariable("userId") Long userId);
}
