package ru.practicum.shareit.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ItemValidator {
    private final ValidationUtils validationUtils;
    private final BookingRepository bookingRepository;

    public void validateItemAccess(Item item, Long userId) {
        validationUtils.checkOwnerAccess(item, userId);
    }

    public void validateCommentCreation(Long userId, Long itemId) {
        validationUtils.getExistingUser(userId);
        validationUtils.getExistingItem(itemId);

        boolean hasBooking = bookingRepository.hasApprovedPastBooking(userId, itemId, LocalDateTime.now());
        if (!hasBooking) {
            throw new ValidationException("Нельзя оставить комментарий: вы не брали эту вещь в аренду");
        }
    }
}