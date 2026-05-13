package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.AccessDeniedException;
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
public class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    public void createBooking_Success() {
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
    public void createBooking_ItemNotFound_ThrowsException() {
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));
        BookingCreateDto createDto = new BookingCreateDto(
                999L, // несуществующий item
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(booker.getId(), createDto));
    }

    @Test
    public void updateBookingStatus_Approve_Success() {
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
    public void getAllBookingsByBooker_WithStateFilter() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto futureDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(11));
        bookingService.createBooking(booker.getId(), futureDto);

        var futureBookings = bookingService.getAllBookingsByBooker(booker.getId(), BookingState.FUTURE, 0, 10);

        assertFalse(futureBookings.isEmpty());
    }

    @Test
    public void updateBookingStatus_Reject_Success() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        BookingResponseDto booking = bookingService.createBooking(booker.getId(), createDto);

        BookingResponseDto rejected = bookingService.updateBookingStatus(booking.getId(), owner.getId(), false);

        assertEquals("REJECTED", rejected.getStatus().name());
    }

    @Test
    public void getBooking_AsBooker_Success() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        BookingResponseDto created = bookingService.createBooking(booker.getId(), createDto);

        BookingResponseDto found = bookingService.getBooking(booker.getId(), created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(booker.getId(), found.getBooker().getId());
    }

    @Test
    public void getBooking_AsOwner_Success() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        BookingResponseDto created = bookingService.createBooking(booker.getId(), createDto);

        BookingResponseDto found = bookingService.getBooking(owner.getId(), created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    public void getBooking_AsThirdParty_ThrowsAccessDenied() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));
        UserDto thirdParty = userService.addUser(new UserDto(null, "Third", "third@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        BookingResponseDto created = bookingService.createBooking(booker.getId(), createDto);

        assertThrows(AccessDeniedException.class, () ->
                bookingService.getBooking(thirdParty.getId(), created.getId()));
    }

    @Test
    public void getAllBookingsByBooker_UserNotFound_ThrowsException() {
        assertThrows(NotFoundException.class, () ->
                bookingService.getAllBookingsByBooker(999L, BookingState.ALL, 0, 10));
    }

    @Test
    public void getAllBookingsByOwner_UserNotFound_ThrowsException() {
        assertThrows(NotFoundException.class, () ->
                bookingService.getAllBookingsByOwner(999L, BookingState.ALL, 0, 10));
    }

    @Test
    public void getAllBookingsByBooker_WithFutureState() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto futureDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(11));
        bookingService.createBooking(booker.getId(), futureDto);

        var futureBookings = bookingService.getAllBookingsByBooker(booker.getId(), BookingState.FUTURE, 0, 10);

        assertFalse(futureBookings.isEmpty());
        assertEquals(1, futureBookings.size());
    }

    @Test
    public void getAllBookingsByOwner_WithAllStates() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        BookingCreateDto createDto = new BookingCreateDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        bookingService.createBooking(booker.getId(), createDto);

        var bookings = bookingService.getAllBookingsByOwner(owner.getId(), BookingState.ALL, 0, 10);

        assertFalse(bookings.isEmpty());
    }
}