package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItems(Long userId);

    ItemDto getItem(Long itemId);

    Collection<ItemDto> search(String text);

    ItemDto addItem(Long userId, ItemDto newItemDto);

    ItemDto updateItem(Long itemId, Long userId, ItemUpdateDto newItemDto);

    void deleteItem(Long itemId, Long userId);

    CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentDto);

    List<CommentDto> getComments(Long itemId);
}