package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.Collection;

public interface BookingService {
    BookingResponseDto createBooking(Long bookerId, BookingCreateDto dto);

    BookingResponseDto updateBookingStatus(Long bookingId, Long ownerId, Boolean approved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    Collection<BookingResponseDto> getAllBookingsByBooker(Long bookerId, BookingState state, Integer from, Integer size);

    Collection<BookingResponseDto> getAllBookingsByOwner(Long ownerId, BookingState state, Integer from, Integer size);
}