package ru.practicum.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.exception.DuplicateFieldException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_shouldSaveUserToDatabase() {
        // Arrange
        UserDto userDto = new UserDto(null, "Ivan", "ivan@yandex.ru");

        // Act
        UserDto result = userService.createUser(userDto);

        // Assert
        assertNotNull(result.getId());
        assertEquals("Ivan", result.getName());
        assertEquals("ivan@yandex.ru", result.getEmail());

        User savedUser = userRepository.findById(result.getId()).orElseThrow();
        assertEquals("Ivan", savedUser.getName());
    }

    @Test
    void createUser_shouldThrowWhenEmailExists() {
        // Arrange
        userRepository.save(new User("Existing", "existing@mail.ru"));
        UserDto userDto = new UserDto(null, "New", "existing@mail.ru");

        // Act & Assert
        assertThrows(DuplicateFieldException.class, () -> userService.createUser(userDto));
    }

    @Test
    void getUser_shouldReturnUser() {
        // Arrange
        User savedUser = userRepository.save(new User("Petr", "petr@mail.ru"));

        // Act
        UserDto result = userService.getUser(savedUser.getId());

        // Assert
        assertEquals(savedUser.getId(), result.getId());
        assertEquals("Petr", result.getName());
        assertEquals("petr@mail.ru", result.getEmail());
    }

    @Test
    void getUser_shouldThrowWhenUserNotFound() {
        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.getUser(999L));
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Arrange
        userRepository.save(new User("User1", "user1@test.ru"));
        userRepository.save(new User("User2", "user2@test.ru"));

        // Act
        List<UserDto> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User1")));
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User2")));
    }

    @Test
    void updateUser_shouldUpdateUserFields() {
        // Arrange
        User savedUser = userRepository.save(new User("OldName", "old@email.ru"));
        UpdateUserDto updateDto = new UpdateUserDto(savedUser.getId(), "NewName", "new@email.ru");

        // Act
        UserDto result = userService.updateUser(updateDto, savedUser.getId());

        // Assert
        assertEquals("NewName", result.getName());
        assertEquals("new@email.ru", result.getEmail());

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertEquals("NewName", updatedUser.getName());
        assertEquals("new@email.ru", updatedUser.getEmail());
    }

    @Test
    void updateUser_shouldThrowWhenEmailExists() {
        // Arrange
        userRepository.save(new User("Existing", "existing@mail.ru"));
        User savedUser = userRepository.save(new User("OldName", "old@email.ru"));
        UpdateUserDto updateDto = new UpdateUserDto(savedUser.getId(), "NewName", "existing@mail.ru");

        // Act & Assert
        assertThrows(DuplicateFieldException.class,
                () -> userService.updateUser(updateDto, savedUser.getId()));
    }

    @Test
    void deleteUser_shouldRemoveUserFromDatabase() {
        // Arrange
        User savedUser = userRepository.save(new User("ToDelete", "delete@me.ru"));

        // Act
        userService.deleteUser(savedUser.getId());

        // Assert
        assertFalse(userRepository.existsById(savedUser.getId()));
    }

    @Test
    void validateUserExist_shouldReturnUserWhenExists() {
        // Arrange
        User savedUser = userRepository.save(new User("Valid", "valid@user.ru"));

        // Act
        User result = userService.validateUserExist(savedUser.getId());

        // Assert
        assertEquals(savedUser.getId(), result.getId());
    }

    @Test
    void validateUserExist_shouldThrowWhenUserNotExists() {
        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.validateUserExist(999L));
    }
}
