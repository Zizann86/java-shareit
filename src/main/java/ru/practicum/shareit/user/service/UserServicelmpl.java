package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateFieldException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.user.mapper.UserMapper.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServicelmpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        validateEmailExists(userDto.getEmail());
        User user = fromDto(userDto);
        user = userRepository.createUser(user);
        log.info("Пользователь создан: {}", user);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto getUser(Long userId) {
        Optional<User> user = userRepository.getUser(userId);
        if (user.isPresent()) {
            log.info("Пользователь с id:{} получен", userId);
            return UserMapper.toDto(user.get());
        } else {
            throw new NotFoundException("Пользователь с данным id не был найден");
        }
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.getAllUsers();
        log.info("Пользователи получены");
        return UserMapper.toDto(users);
    }

    @Override
    public UserDto updateUser(UpdateUserDto userDto, Long userId) {
        User user = validateUserExist(userId);
        validateEmailExists(userDto.getEmail());
        updatefromDto(user, userDto);
        userRepository.updateUser(user);
        log.info("Пользователь с id: {} успешно обновлен", userId);
        return toDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        validateUserExist(userId);
        userRepository.deleteUser(userId);
        log.info("Пользователь с id: {} успешно удален", userId);
    }

    @Override
    public User validateUserExist(Long userId) {
        return userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден.", userId)));
    }

    private void validateEmailExists(String email) {
        List<String> emails = userRepository.getAllUsers().stream()
                .map(User::getEmail)
                .toList();
        if (emails.contains(email)) {
            throw new DuplicateFieldException("Данная электронная почта уже занята");
        }
    }
}
