package ru.practicum.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Collection<UserDto>> findAll(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(userService.findAll(ids, from, size));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<UserDto> add(@Valid @RequestBody NewUserRequest newUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUser(newUserRequest));
    }

    @DeleteMapping("/{userId}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable("userId") Long userId) {
        userService.delete(userId);

        return ResponseEntity.noContent().build();
    }

}
