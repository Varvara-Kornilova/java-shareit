package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody @Valid ItemRequestCreateDto createDto) {
        log.info("POST /requests: создание запроса вещи пользователем {}", userId);
        return itemRequestClient.createRequest(userId, createDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET /requests: список запросов пользователя {}", userId);
        return itemRequestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET /requests/all: список всех запросов, кроме своих, для пользователя {}", userId);
        return itemRequestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET /requests/{}: получение запроса по id", requestId);
        return itemRequestClient.getRequestById(requestId, userId);
    }
}