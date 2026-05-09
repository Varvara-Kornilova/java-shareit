package ru.practicum.shareit.booking.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {
    public static Booking toBooking(BookingCreateDto dto) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        return booking;
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        BookingResponseDto.ItemForBookingDto item = new BookingResponseDto.ItemForBookingDto(
                booking.getItem().getId(),
                booking.getItem().getName()
        );
        BookingResponseDto.BookerDto booker = new BookingResponseDto.BookerDto(
                booking.getBooker().getId(),
                booking.getBooker().getName()
        );
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                item,
                booker
        );
    }

    public static List<BookingResponseDto> toResponseDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}