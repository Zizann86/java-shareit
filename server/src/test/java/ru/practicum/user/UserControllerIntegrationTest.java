package ru.practicum.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserDto userDto = new UserDto(null, "Ivan", "ivan@yandex.ru");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@yandex.ru"));
    }

    @Test
    void getUser_shouldReturnUser() throws Exception {
        User savedUser = userRepository.save(new User("Petr", "petr@mail.ru"));

        mockMvc.perform(get("/users/{userId}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Petr"))
                .andExpect(jsonPath("$.email").value("petr@mail.ru"));
    }

    @Test
    void getAllUsers_shouldReturnUserList() throws Exception {
        userRepository.save(new User("User1", "user1@test.ru"));
        userRepository.save(new User("User2", "user2@test.ru"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    void updateUser_shouldUpdateFields() throws Exception {
        User savedUser = userRepository.save(new User("OldName", "old@email.ru"));
        UpdateUserDto updateDto = new UpdateUserDto(savedUser.getId(), "NewName", "new@email.ru");

        mockMvc.perform(patch("/users/{userId}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.email").value("new@email.ru"));
    }

    @Test
    void deleteUser_shouldRemoveUser() throws Exception {
        User savedUser = userRepository.save(new User("ToDelete", "delete@me.ru"));

        mockMvc.perform(delete("/users/{userId}", savedUser.getId()))
                .andExpect(status().isOk());

        assertFalse(userRepository.existsById(savedUser.getId()));
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
