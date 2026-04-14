package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Collection<ItemDto> getAllItems(Long userId) {
        log.debug("Запрошен список всех предметов пользователя");
        if (userRepository.findUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        Collection<ItemDto> items = itemRepository.findAll()
                .stream()
                .filter(item -> item.getOwner() != null
                                    && item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.debug("Найдено {} пользователей", items.size());
        return items;
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        log.debug("Отправляем запрос на получение вещи по ID {}", itemId);
        if (userRepository.findUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        Item item = itemRepository.findItemById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с id #{} не найдена", itemId);
                    return new NotFoundException("Вещь с таким id не найдена");
                });
        log.debug("Вещь с id #{} найдена", itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> search(Long userId, String text) {
        log.debug("Поиск вещи по названию '{}'", text);
        if (userRepository.findUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        if (text == null || text.isBlank()) {
            log.debug("Текстовый запрос пуст");
            return List.of();
        }

        Collection<ItemDto> itemsByText = itemRepository.findAll()
                .stream()
                .filter(item -> item.getAvailable().equals(Boolean.TRUE))
                .filter(item -> item.getOwner() != null
                                    && item.getOwner().getId().equals(userId))
                .filter(item -> (item.getName() != null
                                    && item.getName().toLowerCase().contains(text.toLowerCase()))
                                    || (item.getDescription() != null
                                    && item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        log.debug("Вещи по текстовому запросу '{}' найдены", text);

        return itemsByText;
    }

    @Override
    public ItemDto addItem(Long userId, ItemDto newItemDto) {
        log.debug("Запрос на добавление новой вещи {} пользователем с id = {}", newItemDto.getName(), userId);

        User owner = userRepository.findUserById(userId).orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));

        Item item = ItemMapper.toItem(newItemDto);

        item.setOwner(owner);

        itemRepository.save(item);
        log.debug("Вещь с id {} успешно добавлена", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemUpdateDto updateItem(Long userId, Long itemId, ItemUpdateDto newItemDto) {
        log.debug("Отправляем запрос на обновление вещи с ID {}", itemId);
        if (userRepository.findUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        Item item = itemRepository.findItemById(itemId)
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

        return ItemMapper.toItemUpdateDto(updatedItem);
    }

    @Override
    public void deleteItemById(Long userId, Long itemId) {
        log.debug("Удаление вещи с id = {} у пользователя с id = {}", itemId, userId);

        if (userRepository.findUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        if (itemRepository.findItemById(itemId).isEmpty()) {
            log.warn("Попытка удаления несуществующей вещи с id={}", itemId);
            throw new NotFoundException("Вещь с id = " + itemId + " не найдена");
        }

        itemRepository.delete(itemId);
        log.info("Вещь с id={} успешно удалена", itemId);
    }
}