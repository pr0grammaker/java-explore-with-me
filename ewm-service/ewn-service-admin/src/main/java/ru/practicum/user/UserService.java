package ru.practicum.user;

import java.util.Collection;
import java.util.List;

public interface UserService {

    UserDto addUser(NewUserRequest newUserRequest);

    Collection<UserDto> findAll(List<Long> ids, int from, int size);

    void delete(Long userId);
}
