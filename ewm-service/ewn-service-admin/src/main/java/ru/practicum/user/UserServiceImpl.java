package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.exceptions.DuplicatedDataException;
import ru.practicum.exceptions.NotFoundException;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto addUser(NewUserRequest newUserRequest) {
        if (userRepository.existsByName(newUserRequest.getName())) {
            throw new DuplicatedDataException("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new DuplicatedDataException("Пользователь с таким email уже существует");
        }

        User user = userMapper.mapToUser(newUserRequest);

        User save = userRepository.save(user);

        return userMapper.mapToUserDto(save);

    }

    @Override
    public Collection<UserDto> findAll(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        List<User> users = userRepository.findAllByIds(ids, pageable);

        return users.stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        userRepository.deleteById(userId);
    }
}
