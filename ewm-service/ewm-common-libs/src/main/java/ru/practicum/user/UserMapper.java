package ru.practicum.user;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User mapToUser(NewUserRequest newUserRequest);

    UserDto mapToUserDto(User save);
}
