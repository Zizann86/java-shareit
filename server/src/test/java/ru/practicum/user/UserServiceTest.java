package ru.practicum.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicateFieldException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServicelmpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    private final UserDto newUserDto = new UserDto(null, "John", "john@example.com");
    private final User savedUser = new User(1L, "John", "john@example.com");
    private final UpdateUserDto updateUserDto = new UpdateUserDto(1L, "NewName", "new@example.com");
    private final User updatedUser = new User(1L, "NewName", "new@example.com");

    @BeforeEach
    void setUp() {
        userService = new UserServicelmpl(userRepository);
    }

    @Test
    void createUser_shouldCreateNewUser() {
        User expectedToSave = new User(null, "John", "john@example.com");
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.save(expectedToSave)).thenReturn(savedUser);

        UserDto result = userService.createUser(newUserDto);

        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
        verify(userRepository).save(expectedToSave);
    }

    @Test
    void createUser_shouldThrowWhenEmailExists() {
        User existingUser = new User(2L, "Existing", "existing@example.com");
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        UserDto duplicateEmailUser = new UserDto(null, "John", "existing@example.com");

        assertThrows(DuplicateFieldException.class,
                () -> userService.createUser(duplicateEmailUser));
    }

    @Test
    void getUser_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        UserDto result = userService.getUser(1L);

        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
    }

    @Test
    void getUser_shouldThrowWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.getUser(999L));
    }

    @Test
    void updateUser_shouldUpdateFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        UserDto result = userService.updateUser(updateUserDto, 1L);

        assertEquals(1L, result.getId());
        assertEquals("NewName", result.getName());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).save(updatedUser);
    }

    @Test
    void deleteUser_shouldCallRepository() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void validateUserExist_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        User result = userService.validateUserExist(1L);

        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
    }

    @Test
    void getAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(savedUser));

        List<UserDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("John", result.get(0).getName());
    }
}
