package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;

public interface ItemService {
    Collection<ItemDto> getAllItems(Long userId);

    ItemDto getItemById(Long userId, Long itemId);

    Collection<ItemDto> search(Long userId, String text);

    ItemDto addItem(Long userId, ItemDto newItemDto);

    ItemUpdateDto updateItem(Long userId, Long itemId, ItemUpdateDto newItemDto);

    void deleteItemById(Long userId, Long itemId);
}