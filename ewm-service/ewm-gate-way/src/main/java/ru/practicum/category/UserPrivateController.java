package ru.practicum.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.http.client.UserPrivateHttpClient;
import ru.practicum.user.NewUserRequest;
import ru.practicum.user.UserDto;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserPrivateController {
    private final UserPrivateHttpClient userPrivateHttpClient;

    @GetMapping
    public ResponseEntity<Collection<UserDto>> findAll(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(userPrivateHttpClient.findAll(ids, from, size).getBody());
    }

    @PostMapping
    public ResponseEntity<UserDto> add(@Valid @RequestBody NewUserRequest newUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userPrivateHttpClient.add(newUserRequest).getBody());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable("userId") Long userId) {
        userPrivateHttpClient.delete(userId);

        return ResponseEntity.noContent().build();
    }
}
