package ru.practicum.shareit.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ValidationUtils {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public User getExistingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    public Item getExistingItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    public void checkOwnerAccess(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Только владелец вещи может выполнять это действие");
        }
    }

    public void checkDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new ValidationException("Некорректные даты: start должен быть раньше end");
        }
    }

    public void checkNotInPast(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Нельзя создать запись в прошлом");
        }
    }
}