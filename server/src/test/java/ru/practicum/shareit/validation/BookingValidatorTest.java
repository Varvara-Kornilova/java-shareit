package ru.practicum.shareit.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingValidatorTest {

    @Mock
    private ValidationUtils validationUtils;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingValidator bookingValidator;

    @Test
    void validateCreateBooking_Success() {
        User booker = new User();
        booker.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setAvailable(true);
        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);

        BookingCreateDto dto = new BookingCreateDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(validationUtils.getExistingUser(1L)).thenReturn(booker);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);

        assertDoesNotThrow(() -> bookingValidator.validateCreateBooking(1L, dto));
    }

    @Test
    void validateCreateBooking_BookingOwnItem_ThrowsException() {
        User booker = new User();
        booker.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setAvailable(true);
        item.setOwner(booker);

        BookingCreateDto dto = new BookingCreateDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(validationUtils.getExistingUser(1L)).thenReturn(booker);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingValidator.validateCreateBooking(1L, dto));
        assertEquals("Нельзя забронировать свою вещь", ex.getMessage());
    }

    @Test
    void validateCreateBooking_ItemNotAvailable_ThrowsException() {
        User booker = new User();
        booker.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setAvailable(false); // недоступно
        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);

        BookingCreateDto dto = new BookingCreateDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(validationUtils.getExistingUser(1L)).thenReturn(booker);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingValidator.validateCreateBooking(1L, dto));
        assertEquals("Вещь недоступна для бронирования", ex.getMessage());
    }

    @Test
    void validateCreateBooking_StartAfterEnd_ThrowsException() {
        User booker = new User();
        booker.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setAvailable(true);
        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);

        BookingCreateDto dto = new BookingCreateDto(10L,
                LocalDateTime.now().plusDays(2), // start позже end
                LocalDateTime.now().plusDays(1));

        when(validationUtils.getExistingUser(1L)).thenReturn(booker);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);
        doThrow(new ValidationException("Некорректные даты"))
                .when(validationUtils).checkDateRange(any(), any());

        assertThrows(ValidationException.class,
                () -> bookingValidator.validateCreateBooking(1L, dto));
    }

    @Test
    void validateUpdateStatus_Success() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.WAITING);
        Item item = new Item();
        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);
        booking.setItem(item);

        assertDoesNotThrow(() -> bookingValidator.validateUpdateStatus(booking, 2L));
        verify(validationUtils).checkOwnerAccess(item, 2L);
    }

    @Test
    void validateUpdateStatus_StatusNotWaiting_ThrowsException() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.APPROVED);
        Item item = new Item();
        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);
        booking.setItem(item);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingValidator.validateUpdateStatus(booking, 2L));
        assertEquals("Статус можно изменить только для бронирований в статусе WAITING", ex.getMessage());
    }

    @Test
    void validateAccess_AsBooker_Success() {
        User booker = new User();
        booker.setId(1L);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);

        assertDoesNotThrow(() -> bookingValidator.validateAccess(booking, 1L));
    }

    @Test
    void validateAccess_AsOwner_Success() {
        User booker = new User();
        booker.setId(1L);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);

        assertDoesNotThrow(() -> bookingValidator.validateAccess(booking, 2L));
    }

    @Test
    void validateAccess_AsThirdParty_ThrowsNotFoundException() {
        User booker = new User();
        booker.setId(1L);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingValidator.validateAccess(booking, 3L));
        assertEquals("Доступ запрещён", ex.getMessage());
    }
}