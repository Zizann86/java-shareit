package ru.practicum.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserById() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");

        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void shouldFindByEmail() {
        User user = new User();
        user.setName("Email Test");
        user.setEmail("unique@example.com");
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("unique@example.com");

        assertTrue(result.isPresent());
        assertEquals("Email Test", result.get().getName());
        assertEquals("unique@example.com", result.get().getEmail());
    }

    @Test
    void shouldNotAllowDuplicateEmails() {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("duplicate@example.com");
        userRepository.saveAndFlush(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("duplicate@example.com");

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }
}
