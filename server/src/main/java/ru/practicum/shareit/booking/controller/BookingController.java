package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody BookingCreateDto requestDto) {
        log.debug("POST /bookings: создание бронирования, userId={}, itemId={}",
                userId, requestDto.getItemId());
        return ResponseEntity.ok(bookingService.createBooking(userId, requestDto));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBookingStatus(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        log.debug("PATCH /bookings/{}: обновление статуса, approved={}", bookingId, approved);
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, ownerId, approved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable Long bookingId) {
        log.debug("GET /bookings/{}: получение бронирования, userId={}", bookingId, userId);
        return ResponseEntity.ok(bookingService.getBooking(userId, bookingId));
    }

    @GetMapping
    public ResponseEntity<Collection<BookingResponseDto>> getAllBookingsByBooker(
            @RequestHeader("X-Sharer-User-Id") long bookerId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.debug("GET /bookings: список бронирований, bookerId={}, state={}", bookerId, state);
        return ResponseEntity.ok(bookingService.getAllBookingsByBooker(bookerId, state, from, size));
    }

    @GetMapping("/owner")
    public ResponseEntity<Collection<BookingResponseDto>> getAllBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.debug("GET /bookings/owner: список бронирований вещей, ownerId={}, state={}", ownerId, state);
        return ResponseEntity.ok(bookingService.getAllBookingsByOwner(ownerId, state, from, size));
    }
}