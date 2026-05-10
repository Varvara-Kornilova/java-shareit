package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;

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

    @Test
    void getAllUsers_ReturnsAllUsers() {
        userService.addUser(new UserDto(null, "User One", "one@test.com"));
        userService.addUser(new UserDto(null, "User Two", "two@test.com"));
        userService.addUser(new UserDto(null, "User Three", "three@test.com"));

        Collection<UserDto> users = userService.getAllUsers();

        assertEquals(3, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("one@test.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("two@test.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("three@test.com")));
    }

    @Test
    void getAllUsers_ReturnsEmpty_WhenNoUsers() {
        Collection<UserDto> users = userService.getAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    void getUserById_Success() {
        UserDto created = userService.addUser(new UserDto(null, "Find Me", "findme@test.com"));

        UserDto found = userService.getUserById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Find Me", found.getName());
        assertEquals("findme@test.com", found.getEmail());
    }

    @Test
    void deleteUserById_Success() {
        UserDto created = userService.addUser(new UserDto(null, "To Delete", "delete@test.com"));
        Long userId = created.getId();

        assertNotNull(userService.getUserById(userId));

        userService.deleteUserById(userId);

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
    }
}