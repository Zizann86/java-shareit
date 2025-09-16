package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto getUser(Long userId);

    List<UserDto> getAllUsers();

    UserDto updateUser(UpdateUserDto userDto, Long userId);

    void deleteUser(Long userId);

    User validateUserExist(Long userId);
}
