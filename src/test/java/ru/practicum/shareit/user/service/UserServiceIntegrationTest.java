package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void addUser_Success() {
        UserDto newUser = new UserDto(null, "Test User", "test@test.com");

        UserDto created = userService.addUser(newUser);

        assertNotNull(created.getId());
        assertEquals("Test User", created.getName());
        assertEquals("test@test.com", created.getEmail());
    }

    @Test
    void addUser_WithDuplicateEmail_ThrowsException() {
        UserDto user1 = new UserDto(null, "User1", "duplicate@test.com");
        UserDto user2 = new UserDto(null, "User2", "duplicate@test.com");

        userService.addUser(user1);

        assertThrows(DuplicatedDataException.class, () -> userService.addUser(user2));
    }

    @Test
    void updateUser_Success() {
        UserDto user = userService.addUser(new UserDto(null, "Old Name", "old@test.com"));
        UserUpdateDto updateDto = new UserUpdateDto(user.getId(), "New Name", "new@test.com");

        UserUpdateDto updated = userService.updateUser(user.getId(), updateDto);

        assertEquals("New Name", updated.getName());
        assertEquals("new@test.com", updated.getEmail());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }
}