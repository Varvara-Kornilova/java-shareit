package ru.practicum.shareit.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

@Component
@RequiredArgsConstructor
public class BookingValidator {
    private final ValidationUtils validationUtils;
    private final ItemRepository itemRepository;

    public void validateCreateBooking(Long bookerId, BookingCreateDto dto) {
        validationUtils.getExistingUser(bookerId);
        Item item = validationUtils.getExistingItem(dto.getItemId());

        if (item.getOwner().getId().equals(bookerId)) {
            throw new ValidationException("Нельзя забронировать свою вещь");
        }
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        validationUtils.checkDateRange(dto.getStart(), dto.getEnd());
        validationUtils.checkNotInPast(dto.getStart());
    }

    public void validateUpdateStatus(Booking booking, Long ownerId) {
        validationUtils.checkOwnerAccess(booking.getItem(), ownerId);

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус можно изменить только для бронирований в статусе WAITING");
        }
    }

    public void validateAccess(Booking booking, Long userId) {
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Доступ запрещён");
        }
    }
}