package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final long user_id = 1L;

    @Test
    void getAllUsers_Success() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_Success() throws Exception {
        UserDto user = new UserDto(user_id, "Иван", "ivan@test.com");
        when(userService.getUserById(eq(user_id))).thenReturn(user);

        mockMvc.perform(get("/users/{userId}", user_id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user_id))
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.email").value("ivan@test.com"));
    }

    @Test
    void getUserById_NotFound_ThrowsException() throws Exception {
        when(userService.getUserById(eq(user_id)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/users/{userId}", user_id))
                .andExpect(status().isNotFound());
    }

    @Test
    void addUser_Success() throws Exception {
        UserDto newUser = new UserDto(null, "Новый", "new@test.com");
        UserDto created = new UserDto(user_id, "Новый", "new@test.com");

        when(userService.addUser(any(UserDto.class))).thenReturn(created);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(user_id))
                .andExpect(jsonPath("$.email").value("new@test.com"));

        verify(userService).addUser(any(UserDto.class));
    }

    @Test
    void addUser_DuplicateEmail_ThrowsException() throws Exception {
        UserDto newUser = new UserDto(null, "Новый", "exists@test.com");

        when(userService.addUser(any(UserDto.class)))
                .thenThrow(new DuplicatedDataException("Email уже используется"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_Success() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto(user_id, "Обновлённый", "updated@test.com");
        UserUpdateDto updated = new UserUpdateDto(user_id, "Обновлённый", "updated@test.com");

        when(userService.updateUser(eq(user_id), any(UserUpdateDto.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/users/{userId}", user_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновлённый"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUserById(eq(user_id));

        mockMvc.perform(delete("/users/{userId}", user_id))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserById(eq(user_id));
    }
}