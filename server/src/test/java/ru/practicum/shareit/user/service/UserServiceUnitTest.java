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
    void updateUser_PartialUpdate_OnlyName() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Старое имя");
        existing.setEmail("old@test.com");

        UserUpdateDto updateDto = new UserUpdateDto(1L, "Новое имя", null); // email = null

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = userService.updateUser(1L, updateDto);

        assertEquals("Новое имя", result.getName());
        assertEquals("old@test.com", result.getEmail()); // email не изменился
        verify(repository).save(existing);
    }

    @Test
    void updateUser_EmailChanged_ValidateUnique() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@test.com");

        UserUpdateDto updateDto = new UserUpdateDto(1L, "Имя", "new@test.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> userService.updateUser(1L, updateDto));
        verify(repository).findByEmail("new@test.com");
    }

    @Test
    void updateUser_EmailChanged_ToExisting_ThrowsException() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("old@test.com");

        User other = new User();
        other.setId(2L);
        other.setEmail("new@test.com");

        UserUpdateDto updateDto = new UserUpdateDto(1L, "Имя", "new@test.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("new@test.com")).thenReturn(Optional.of(other));

        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> userService.updateUser(1L, updateDto));
        assertTrue(ex.getMessage().contains("уже используется"));
    }

    @Test
    void updateUser_EmailChanged_ToSameUser_DoesNotThrow() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("same@test.com");

        UserUpdateDto updateDto = new UserUpdateDto(1L, "Имя", "same@test.com"); // тот же email

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> userService.updateUser(1L, updateDto));

        verify(repository, never()).findByEmail(anyString());
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