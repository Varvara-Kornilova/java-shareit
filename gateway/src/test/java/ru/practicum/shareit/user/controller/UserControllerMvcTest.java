package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;  // ✅ Мокаем клиент

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addUser_WithValidData_ShouldReturn200() throws Exception {
        UserDto newUser = new UserDto(null, "Test User", "test@test.com");
        UserDto created = new UserDto(1L, "Test User", "test@test.com");

        when(userClient.addUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(created));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void addUser_WithInvalidEmail_ShouldReturn400() throws Exception {
        UserDto invalidUser = new UserDto(null, "Test", "invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_WithDuplicateEmail_ShouldReturn409() throws Exception {
        UserDto existingUser = new UserDto(1L, "Existing", "dup@test.com");
        UserDto newUser = new UserDto(null, "New", "dup@test.com");

        // Первый вызов — успех, второй — конфликт
        when(userClient.addUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(existingUser))
                .thenReturn(ResponseEntity.status(409).body("Email exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingUser)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_PartialUpdate_ShouldReturn200() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto(1L, "New Name", null);
        UserUpdateDto updated = new UserUpdateDto(1L, "New Name", "old@test.com");

        when(userClient.updateUser(eq(1L), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(updated));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }
}