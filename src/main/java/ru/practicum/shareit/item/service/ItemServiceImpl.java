package ru.practicum.shareit.item.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.validation.ValidationUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ValidationUtils validationUtils;
    private final ItemRequestRepository requestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems(Long userId) {
        log.debug("Запрошен список всех предметов пользователя {}", userId);

        validationUtils.getExistingUser(userId);

        List<Item> items = itemRepository.findByOwnerId(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);
                    dto.setLastBooking(findLastBookingForItem(item.getId(), now));
                    dto.setNextBooking(findNextBookingForItem(item.getId(), now));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long itemId) {
        log.debug("Отправляем запрос на получение вещи по ID {}", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с id #{} не найдена", itemId);
                    return new NotFoundException("Вещь с таким id не найдена");
                });

        ItemDto dto = ItemMapper.toItemDto(item);
        List<CommentDto> comments = commentRepository
                .findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        dto.setComments(comments);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchByText(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto newItemDto) {
        log.debug("Запрос на добавление новой вещи {} пользователем с id = {}",
                newItemDto.getName(), userId);

        User owner = validationUtils.getExistingUser(userId);

        if (newItemDto.getRequestId() != null) {
            requestRepository.findById(newItemDto.getRequestId())
                    .orElseThrow(() -> {
                        log.warn("Запрос с id #{} не найден", newItemDto.getRequestId());
                        return new NotFoundException("Запрос с таким id не найден");
                    });
        }

        Item item = ItemMapper.toItem(newItemDto);
        item.setOwner(owner);

        item.setRequestId(newItemDto.getRequestId());

        Item saved = itemRepository.save(item);
        log.debug("Вещь с id {} успешно добавлена", saved.getId());
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto newItemDto) {
        log.debug("Запрос на обновление вещи с ID {}", itemId);

        validationUtils.getExistingUser(userId);
        Item item = validationUtils.getExistingItem(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь с id=" + itemId + " не принадлежит пользователю");
        }

        if (newItemDto.getName() != null) {
            item.setName(newItemDto.getName());
        }
        if (newItemDto.getDescription() != null) {
            item.setDescription(newItemDto.getDescription());
        }
        if (newItemDto.getAvailable() != null) {
            item.setAvailable(newItemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        log.debug("Вещь с id = {} успешно обновлена", itemId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItemById(Long userId, Long itemId) {
        log.debug("Удаление вещи с id = {} у пользователя с id = {}", itemId, userId);

        validationUtils.getExistingUser(userId);
        Item item = validationUtils.getExistingItem(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь с id=" + itemId + " не принадлежит пользователю");
        }

        itemRepository.deleteById(itemId);
        log.info("Вещь с id={} успешно удалена", itemId);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentDto) {
        log.debug("Добавление комментария к вещи {}: userId={}, text={}",
                itemId, userId, commentDto.getText());

        User author = validationUtils.getExistingUser(userId);
        Item item = validationUtils.getExistingItem(itemId);

        boolean hasCompletedBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED);

        if (!hasCompletedBooking) {
            throw new ValidationException("Нельзя оставить комментарий: вы не брали эту вещь в аренду");
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author);
        Comment saved = commentRepository.save(comment);

        log.info("Комментарий добавлен: id={}, itemId={}, authorId={}",
                saved.getId(), itemId, userId);
        return CommentMapper.toCommentDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByItemId(Long itemId) {
        validationUtils.getExistingItem(itemId); // проверка существования вещи

        return commentRepository.findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private BookingItemDto findLastBookingForItem(Long itemId, LocalDateTime now) {
        return bookingRepository
                .findLastBookingByItemIdAndStatusBeforeNow(itemId, now, BookingStatus.APPROVED)
                .map(booking -> new BookingItemDto(
                        booking.getId(),
                        booking.getBooker().getId(),
                        booking.getStart(),
                        booking.getEnd()
                ))
                .orElse(null);
    }

    private BookingItemDto findNextBookingForItem(Long itemId, LocalDateTime now) {
        return bookingRepository
                .findNextBookingByItemIdAndStatusAfterNow(itemId, now, BookingStatus.APPROVED)
                .map(booking -> new BookingItemDto(
                        booking.getId(),
                        booking.getBooker().getId(),
                        booking.getStart(),
                        booking.getEnd()
                ))
                .orElse(null);
    }
}