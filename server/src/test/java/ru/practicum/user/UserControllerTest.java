package ru.practicum.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    private final UserDto inputUserDto = new UserDto(null, "name", "email@mail.ru");
    private final UserDto outputUserDto = new UserDto(1L, "name", "email@mail.ru");
    private final UpdateUserDto updateUserDto = new UpdateUserDto(1L, "newName", "new@mail.ru");
    private final UserDto updatedUserDto = new UserDto(1L, "newName", "new@mail.ru");

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        when(userService.createUser(inputUserDto)).thenReturn(outputUserDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(inputUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()) // Изменено с isOk() на isCreated()
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.email").value("email@mail.ru"));

        verify(userService).createUser(inputUserDto);
    }


    @Test
    void getUser_shouldReturnUserById() throws Exception {
        when(userService.getUser(1L)).thenReturn(outputUserDto);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.email").value("email@mail.ru"));

        verify(userService).getUser(1L);
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(outputUserDto));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("name"))
                .andExpect(jsonPath("$[0].email").value("email@mail.ru"));

        verify(userService).getAllUsers();
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        when(userService.updateUser(updateUserDto, 1L)).thenReturn(updatedUserDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updateUserDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("newName"))
                .andExpect(jsonPath("$.email").value("new@mail.ru"));

        verify(userService).updateUser(updateUserDto, 1L);
    }

    @Test
    void deleteUser_shouldCallDeleteMethod() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }
}
