package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Collection<ItemDto>> getAllItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET /items: список вещей пользователя {}", userId);
        return (ResponseEntity<Collection<ItemDto>>) (ResponseEntity<?>) itemClient.getAllItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId) {
        log.info("GET /items/{}: получение вещи", itemId);
        return itemClient.getItem(itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(required = false) String text) {
        log.info("GET /items/search: поиск по тексту '{}'", text);
        return itemClient.search(text);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestBody @Valid ItemDto newItem) {
        log.info("POST /items: добавление вещи пользователем {}", userId);
        return itemClient.addItem(userId, newItem);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Valid ItemUpdateDto newItemDto) {
        log.info("PATCH /items/{}: обновление вещи пользователем {}", itemId, userId);
        return itemClient.updateItem(itemId, userId, newItemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable Long itemId) {
        log.info("DELETE /items/{}: удаление вещи пользователем {}", itemId, userId);
        return itemClient.deleteItem(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Valid CommentCreateDto commentDto) {
        log.info("POST /items/{}/comment: добавление комментария", itemId);
        return itemClient.addComment(itemId, userId, commentDto);
    }

    @GetMapping("/{itemId}/comments")
    public ResponseEntity<Object> getCommentsByItemId(@PathVariable Long itemId) {
        log.info("GET /items/{}/comments: получение комментариев", itemId);
        return itemClient.getComments(itemId);
    }
}