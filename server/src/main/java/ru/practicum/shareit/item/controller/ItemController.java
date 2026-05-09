package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Collection<ItemDto>> getAllItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("GET /items: список вещей пользователя {}", userId);
        return ResponseEntity.ok(itemService.getAllItems(userId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long itemId) {
        log.debug("GET /items/{}: получение вещи", itemId);
        return ResponseEntity.ok(itemService.getItem(itemId));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> search(@RequestParam(required = false) String text) {
        log.debug("GET /items/search: поиск по тексту '{}'", text);
        return ResponseEntity.ok(itemService.search(text));
    }

    @PostMapping
    public ResponseEntity<ItemDto> addItem(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody ItemDto newItem) {
        log.debug("POST /items: добавление вещи, userId={}", userId);
        return ResponseEntity.status(201).body(itemService.addItem(userId, newItem));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto newItemDto) {
        log.debug("PATCH /items/{}: обновление вещи, userId={}", itemId, userId);
        return ResponseEntity.ok(itemService.updateItem(userId, itemId, newItemDto));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable Long itemId) {
        log.debug("DELETE /items/{}: удаление вещи, userId={}", itemId, userId);
        itemService.deleteItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable Long itemId,
            @RequestBody CommentCreateDto commentDto) {
        log.debug("POST /items/{}/comment: добавление комментария, userId={}", itemId, userId);
        return ResponseEntity.ok(itemService.addComment(userId, itemId, commentDto));
    }

    @GetMapping("/{itemId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long itemId) {
        log.debug("GET /items/{}/comments: получение комментариев", itemId);
        return ResponseEntity.ok(itemService.getComments(itemId));
    }
}