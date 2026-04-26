package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long bookerId, BookingCreateDto dto) {
        log.debug("Создание бронирования: userId={}, itemId={}, start={}, end={}",
                bookerId, dto.getItemId(), dto.getStart(), dto.getEnd());

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + bookerId + " не найден"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + dto.getItemId() + " не найдена"));

        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Нельзя забронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (dto.getStart().isAfter(dto.getEnd()) || dto.getStart().isEqual(dto.getEnd())) {
            throw new ValidationException("Некорректные даты бронирования: start должен быть раньше end");
        }
        if (dto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Нельзя создать бронирование в прошлом");
        }

        Booking booking = BookingMapper.toBooking(dto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING); // По умолчанию

        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование создано: id={}", saved.getId());
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long bookingId, Long ownerId, Boolean approved) {
        log.debug("Обновление статуса бронирования: bookingId={}, ownerId={}, approved={}",
                bookingId, ownerId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец вещи может подтвердить или отклонить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования можно изменить только когда он WAITING");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updated = bookingRepository.save(booking);
        log.info("Статус бронирования обновлён: id={}, status={}", bookingId, booking.getStatus());
        return BookingMapper.toBookingResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        log.debug("Получение бронирования: bookingId={}, userId={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Доступ запрещён: вы не являетесь автором бронирования или владельцем вещи");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingResponseDto> getAllBookingsByBooker(Long bookerId, BookingState state) {
        log.debug("Получение списка бронирований пользователя: userId={}, state={}", bookerId, state);

        if (userRepository.findById(bookerId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + bookerId + " не найден");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByBookerIdAndState(bookerId, state, now);
        return BookingMapper.toResponseDtoList(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingResponseDto> getAllBookingsByOwner(Long ownerId, BookingState state) {
        log.debug("Получение списка бронирований вещей владельца: ownerId={}, state={}", ownerId, state);

        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + ownerId + " не найден");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByOwnerIdAndState(ownerId, state, now);
        return BookingMapper.toResponseDtoList(bookings);
    }
}