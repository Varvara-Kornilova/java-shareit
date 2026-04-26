package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    @GetMapping
    public Collection<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getAllItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        return service.getItemById(itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam(required = false) String text) {
        return service.search(text);
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @Valid @RequestBody ItemDto newItem) {
        return service.addItem(userId, newItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId,
                              @Valid @RequestBody ItemUpdateDto newItemDto) {
        return service.updateItem(userId, itemId, newItemDto);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable Long itemId) {
        service.deleteItemById(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentCreateDto commentDto) {
        return service.addComment(userId, itemId, commentDto);
    }

    @GetMapping("/{itemId}/comments")
    public List<CommentDto> getCommentsByItemId(@PathVariable Long itemId) {
        return service.getCommentsByItemId(itemId);
    }
}