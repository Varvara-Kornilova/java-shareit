package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;

    @PostMapping
    public BookingResponseDto createBooking(
            @RequestHeader("X-Sharer-User-Id") Long bookerId,
            @Valid @RequestBody BookingCreateDto dto) {
        log.info("POST /bookings: создание бронирования пользователем {}", bookerId);
        return service.createBooking(bookerId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBookingStatus(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{}: обновление статуса, approved={}", bookingId, approved);
        return service.updateBookingStatus(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {
        log.info("GET /bookings/{}: получение бронирования пользователем {}", bookingId, userId);
        return service.getBookingById(bookingId, userId);
    }

    @GetMapping
    public Collection<BookingResponseDto> getAllBookingsByBooker(
            @RequestHeader("X-Sharer-User-Id") Long bookerId,
            @RequestParam(name = "state", defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings: список бронирований пользователя {}, state={}", bookerId, state);
        return service.getAllBookingsByBooker(bookerId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> getAllBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(name = "state", defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings/owner: список бронирований вещей владельца {}, state={}", ownerId, state);
        return service.getAllBookingsByOwner(ownerId, state);
    }
}