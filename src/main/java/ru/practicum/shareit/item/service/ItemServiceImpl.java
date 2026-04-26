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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems(Long userId) {
        log.debug("Запрошен список всех предметов пользователя");

        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        List<Item> items = itemRepository.findByOwnerId(userId);
        LocalDateTime now = LocalDateTime.now();

        List<ItemDto> itemDtos = items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);

                    // 👇 Добавляем информацию о бронированиях (только для владельца)
                    dto.setLastBooking(findLastBookingForItem(item.getId(), now));
                    dto.setNextBooking(findNextBookingForItem(item.getId(), now));

                    return dto;
                })
                .collect(Collectors.toList());

        log.debug("Найдено {} вещей", itemDtos.size());
        return itemDtos;
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

        log.debug("Вещь с id #{} найдена", itemId);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> search(String text) {
        log.debug("Поиск вещи по названию '{}'", text);

        if (text == null || text.isBlank()) {
            log.debug("Текстовый запрос пуст");
            return List.of();
        }

        List<Item> items = itemRepository.searchByText(text);

        log.debug("Найдено {} вещей по запросу '{}'", items.size(), text);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto newItemDto) {
        log.debug("Запрос на добавление новой вещи {} пользователем с id = {}", newItemDto.getName(), userId);

        User owner = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));

        Item item = ItemMapper.toItem(newItemDto);

        item.setOwner(owner);

        itemRepository.save(item);
        log.debug("Вещь с id {} успешно добавлена", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto newItemDto) {
        log.debug("Отправляем запрос на обновление вещи с ID {}", itemId);

        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("У пользователя нет этой вещи");
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
        log.debug("Вещь с id = {} успешно обновлена у пользователя с id = {}", itemId, userId);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItemById(Long userId, Long itemId) {
        log.debug("Удаление вещи с id = {} у пользователя с id = {}", itemId, userId);

        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        if (itemRepository.findById(itemId).isEmpty()) {
            log.warn("Попытка удаления несуществующей вещи с id={}", itemId);
            throw new NotFoundException("Вещь с id = " + itemId + " не найдена");
        }

        itemRepository.deleteById(itemId);
        log.info("Вещь с id={} успешно удалена", itemId);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentDto) {
        log.debug("Добавление комментария к вещи {}: userId={}, text={}", itemId, userId, commentDto.getText());

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        boolean hasBooking = bookingRepository.hasApprovedPastBooking(userId, itemId, LocalDateTime.now());
        if (!hasBooking) {
            throw new ValidationException("Нельзя оставить комментарий: вы не брали эту вещь в аренду");
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author);
        Comment saved = commentRepository.save(comment);

        log.info("Комментарий добавлен: id={}, itemId={}, authorId={}", saved.getId(), itemId, userId);
        return CommentMapper.toCommentDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByItemId(Long itemId) {
        log.debug("Запрос комментариев для вещи: itemId={}", itemId);

        if (itemRepository.findById(itemId).isEmpty()) {
            throw new NotFoundException("Вещь с id=" + itemId + " не найдена");
        }

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        return comments.stream()
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