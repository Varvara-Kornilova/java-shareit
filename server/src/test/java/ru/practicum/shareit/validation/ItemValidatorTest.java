package ru.practicum.shareit.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemValidatorTest {

    @Mock
    private ValidationUtils validationUtils;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ItemValidator itemValidator;

    @Test
    public void validateItemAccess_Success() {
        User owner = new User();
        owner.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        doNothing().when(validationUtils).checkOwnerAccess(item, 1L);

        assertDoesNotThrow(() -> itemValidator.validateItemAccess(item, 1L));

        verify(validationUtils).checkOwnerAccess(item, 1L);
    }

    @Test
    public void validateItemAccess_NotOwner_ThrowsException() {
        User owner = new User();
        owner.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        doThrow(new RuntimeException("Доступ запрещен"))
                .when(validationUtils).checkOwnerAccess(item, 2L);

        assertThrows(RuntimeException.class, () ->
                itemValidator.validateItemAccess(item, 2L));
    }

    @Test
    public void validateCommentCreation_Success() {
        Long userId = 1L;
        Long itemId = 10L;

        User user = new User();
        user.setId(userId);
        Item item = new Item();
        item.setId(itemId);

        when(validationUtils.getExistingUser(userId)).thenReturn(user);
        when(validationUtils.getExistingItem(itemId)).thenReturn(item);

        when(bookingRepository.hasApprovedPastBooking(eq(userId), eq(itemId), any(LocalDateTime.class)))
                .thenReturn(true);

        assertDoesNotThrow(() -> itemValidator.validateCommentCreation(userId, itemId));

        verify(validationUtils).getExistingUser(userId);
        verify(validationUtils).getExistingItem(itemId);
        verify(bookingRepository).hasApprovedPastBooking(eq(userId), eq(itemId), any(LocalDateTime.class));
    }

    @Test
    public void validateCommentCreation_NoApprovedBooking_ThrowsException() {
        Long userId = 1L;
        Long itemId = 10L;

        User user = new User();
        user.setId(userId);
        Item item = new Item();
        item.setId(itemId);

        when(validationUtils.getExistingUser(userId)).thenReturn(user);
        when(validationUtils.getExistingItem(itemId)).thenReturn(item);

        when(bookingRepository.hasApprovedPastBooking(eq(userId), eq(itemId), any(LocalDateTime.class)))
                .thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                itemValidator.validateCommentCreation(userId, itemId));
        assertTrue(ex.getMessage().contains("не брали эту вещь в аренду"));

        verify(validationUtils).getExistingUser(userId);
        verify(validationUtils).getExistingItem(itemId);
        verify(bookingRepository).hasApprovedPastBooking(eq(userId), eq(itemId), any(LocalDateTime.class));
    }

    @Test
    public void validateCommentCreation_UserNotFound_ThrowsException() {
        Long userId = 1L;
        Long itemId = 10L;

        when(validationUtils.getExistingUser(userId))
                .thenThrow(new RuntimeException("Пользователь не найден"));

        assertThrows(RuntimeException.class, () ->
                itemValidator.validateCommentCreation(userId, itemId));
        verify(validationUtils).getExistingUser(userId);
        verify(validationUtils, never()).getExistingItem(anyLong());
        verify(bookingRepository, never()).hasApprovedPastBooking(anyLong(), anyLong(), any());
    }

    @Test
    public void validateCommentCreation_ItemNotFound_ThrowsException() {
        Long userId = 1L;
        Long itemId = 10L;

        User user = new User();
        user.setId(userId);

        when(validationUtils.getExistingUser(userId)).thenReturn(user);
        when(validationUtils.getExistingItem(itemId))
                .thenThrow(new RuntimeException("Вещь не найдена"));

        assertThrows(RuntimeException.class, () ->
                itemValidator.validateCommentCreation(userId, itemId));
        verify(validationUtils).getExistingUser(userId);
        verify(validationUtils).getExistingItem(itemId);
        verify(bookingRepository, never()).hasApprovedPastBooking(anyLong(), anyLong(), any());
    }
}