package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    void createBooking_Success() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        BookingResponseDto created = bookingService.createBooking(booker.getId(), createDto);

        assertNotNull(created.getId());
        assertEquals("WAITING", created.getStatus().name());
        assertEquals(item.getId(), created.getItem().getId());
        assertEquals(booker.getId(), created.getBooker().getId());
    }

    @Test
    void createBooking_ItemNotFound_ThrowsException() {
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));
        BookingCreateDto createDto = new BookingCreateDto(
                999L, // несуществующий item
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(booker.getId(), createDto));
    }

    @Test
    void updateBookingStatus_Approve_Success() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        BookingResponseDto booking = bookingService.createBooking(booker.getId(), createDto);

        BookingResponseDto updated = bookingService.updateBookingStatus(booking.getId(), owner.getId(), true);

        assertEquals("APPROVED", updated.getStatus().name());
    }

    @Test
    void getAllBookingsByBooker_WithStateFilter() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto futureDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(11));
        bookingService.createBooking(booker.getId(), futureDto);

        var futureBookings = bookingService.getAllBookingsByBooker(booker.getId(), BookingState.FUTURE);

        assertFalse(futureBookings.isEmpty());
    }
}