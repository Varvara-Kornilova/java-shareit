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
import ru.practicum.shareit.validation.BookingValidator;

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
    private final BookingValidator bookingValidator;
    private final BookingMapper bookingMapper;


    @Override
    @Transactional
    public BookingResponseDto createBooking(Long bookerId, BookingCreateDto dto) {
        log.debug("Создание бронирования: userId={}, itemId={}", bookerId, dto.getItemId());

        bookingValidator.validateCreateBooking(bookerId, dto);

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + dto.getItemId() + " не найдена"));
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + bookerId + " не найден"));

        Booking booking = bookingMapper.toBooking(dto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование создано: id={}", saved.getId());
        return bookingMapper.toBookingResponseDto(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long bookingId, Long ownerId, Boolean approved) {
        log.debug("Обновление статуса: bookingId={}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        bookingValidator.validateUpdateStatus(booking, ownerId);

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        bookingValidator.validateAccess(booking, userId);

        return bookingMapper.toBookingResponseDto(booking);
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