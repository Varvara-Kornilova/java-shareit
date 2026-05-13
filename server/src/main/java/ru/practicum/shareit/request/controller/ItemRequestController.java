package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestResponseDto> createRequest(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody ItemRequestCreateDto createDto) {
        log.debug("POST /requests: создание запроса, userId={}", userId);
        return ResponseEntity.status(201).body(itemRequestService.createRequest(userId, createDto));
    }

    @GetMapping
    public ResponseEntity<Collection<ItemRequestResponseDto>> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("GET /requests: список запросов пользователя {}", userId);
        return ResponseEntity.ok(itemRequestService.getUserRequests(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<Collection<ItemRequestResponseDto>> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("GET /requests/all: список всех запросов, кроме своих, для пользователя {}", userId);
        return ResponseEntity.ok(itemRequestService.getAllRequests(userId));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestResponseDto> getRequestById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("GET /requests/{}: получение запроса по id", requestId);
        return ResponseEntity.ok(itemRequestService.getRequestById(requestId, userId));
    }
}
