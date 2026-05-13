package ru.practicum.shareit.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ValidationUtilsTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ValidationUtils validationUtils;

    @Test
    public void getExistingUser_Found_ReturnsUser() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = validationUtils.getExistingUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void getExistingUser_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> validationUtils.getExistingUser(1L));
        assertTrue(ex.getMessage().contains("не найден"));
    }

    @Test
    public void checkOwnerAccess_Owner_Success() {
        Item item = new Item();
        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);

        assertDoesNotThrow(() -> validationUtils.checkOwnerAccess(item, 2L));
    }

    @Test
    public void checkOwnerAccess_NotOwner_ThrowsException() {
        Item item = new Item();
        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> validationUtils.checkOwnerAccess(item, 3L));
        assertTrue(ex.getMessage().contains("Только владелец"));
    }

    @Test
    public void checkDateRange_Valid_DoesNotThrow() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);

        assertDoesNotThrow(() -> validationUtils.checkDateRange(start, end));
    }

    @Test
    public void checkDateRange_StartAfterEnd_ThrowsException() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.minusDays(1);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationUtils.checkDateRange(start, end));
        assertTrue(ex.getMessage().contains("start должен быть раньше end"));
    }

    @Test
    public void checkDateRange_NullStart_ThrowsException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationUtils.checkDateRange(null, LocalDateTime.now()));
        assertTrue(ex.getMessage().contains("Некорректные даты"));
    }

    @Test
    public void checkNotInPast_Future_DoesNotThrow() {
        LocalDateTime future = LocalDateTime.now().plusHours(1);
        assertDoesNotThrow(() -> validationUtils.checkNotInPast(future));
    }

    @Test
    public void checkNotInPast_Past_ThrowsException() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationUtils.checkNotInPast(past));
        assertTrue(ex.getMessage().contains("в прошлом"));
    }
}