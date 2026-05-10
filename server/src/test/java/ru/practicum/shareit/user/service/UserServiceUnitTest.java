package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateUser_OnlyNameChanged_EmailUnchanged() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setEmail("old@test.com");

        UserUpdateDto updateDto = new UserUpdateDto(1L, "New Name", null); // email = null

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = userService.updateUser(1L, updateDto);

        assertEquals("New Name", result.getName());
        assertEquals("old@test.com", result.getEmail()); // email не изменился
        verify(repository, never()).findByEmail(anyString()); // findByEmail не вызывался
    }

    @Test
    void updateUser_OnlyEmailChanged_NameUnchanged() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setEmail("old@test.com");

        UserUpdateDto updateDto = new UserUpdateDto(1L, null, "new@test.com"); // name = null

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("new@test.com")).thenReturn(Optional.empty()); // email свободен
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = userService.updateUser(1L, updateDto);

        assertEquals("Old Name", result.getName()); // name не изменился
        assertEquals("new@test.com", result.getEmail());
        verify(repository).findByEmail("new@test.com");
    }

    @Test
    void updateUser_EmailChanged_ToSameUser_DoesNotThrow() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Имя");
        existing.setEmail("same@test.com");

        UserUpdateDto updateDto = new UserUpdateDto(1L, "Новое имя", "same@test.com"); // тот же email

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> userService.updateUser(1L, updateDto));

        verify(repository, never()).findByEmail(anyString());
    }

    @Test
    void updateUser_EmailChanged_ToOtherUser_ThrowsException() {
        // Проверяем ветку: новый email уже используется другим пользователем → исключение
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@test.com");

        User other = new User();
        other.setId(2L);
        other.setEmail("conflict@test.com");

        UserUpdateDto updateDto = new UserUpdateDto(1L, "Имя", "conflict@test.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("conflict@test.com")).thenReturn(Optional.of(other));

        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> userService.updateUser(1L, updateDto));
        assertTrue(ex.getMessage().contains("уже используется"));
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.deleteUserById(1L));
        assertTrue(ex.getMessage().contains("не найден"));
        verify(repository, never()).deleteById(anyLong());
    }
}