package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    @Test
    void toBooking_MapsFields() {
        BookingCreateDto dto = new BookingCreateDto(10L,
                LocalDateTime.of(2024, 6, 1, 10, 0),
                LocalDateTime.of(2024, 6, 5, 18, 0));

        Booking result = BookingMapper.toBooking(dto);

        assertEquals(dto.getStart(), result.getStart());
        assertEquals(dto.getEnd(), result.getEnd());
        assertNull(result.getItem()); // item устанавливается в сервисе
    }

    @Test
    void toBookingResponseDto_MapsAllFields() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStart(LocalDateTime.of(2024, 6, 1, 10, 0));
        booking.setEnd(LocalDateTime.of(2024, 6, 5, 18, 0));
        booking.setStatus(BookingStatus.APPROVED);

        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        booking.setItem(item);

        User booker = new User();
        booker.setId(1L);
        booker.setName("Иван");
        booking.setBooker(booker);

        BookingResponseDto result = BookingMapper.toBookingResponseDto(booking);

        assertEquals(100L, result.getId());
        assertEquals("Дрель", result.getItem().getName());
        assertEquals("Иван", result.getBooker().getName());
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void toResponseDtoList_EmptyList_ReturnsEmpty() {
        List<BookingResponseDto> result = BookingMapper.toResponseDtoList(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void toResponseDtoList_MultipleBookings_MapsAll() {
        Booking b1 = new Booking();
        b1.setId(1L);
        b1.setStatus(BookingStatus.WAITING);
        b1.setItem(new Item());
        b1.getItem().setId(10L);
        b1.getItem().setName("Item1");
        b1.setBooker(new User());
        b1.getBooker().setId(1L);
        b1.getBooker().setName("User1");

        Booking b2 = new Booking();
        b2.setId(2L);
        b2.setStatus(BookingStatus.REJECTED);
        b2.setItem(new Item());
        b2.getItem().setId(20L);
        b2.getItem().setName("Item2");
        b2.setBooker(new User());
        b2.getBooker().setId(2L);
        b2.getBooker().setName("User2");

        List<BookingResponseDto> result = BookingMapper.toResponseDtoList(List.of(b1, b2));

        assertEquals(2, result.size());
        assertEquals("Item1", result.get(0).getItem().getName());
        assertEquals("User2", result.get(1).getBooker().getName());
    }
}