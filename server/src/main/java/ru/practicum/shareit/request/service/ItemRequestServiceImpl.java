package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.validation.ValidationUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ValidationUtils validationUtils;

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestCreateDto createDto) {
        log.debug("Создание запроса вещи пользователем {}: {}", userId, createDto.getDescription());

        User requestor = validationUtils.getExistingUser(userId);

        ItemRequest request = new ItemRequest();
        request.setDescription(createDto.getDescription());
        request.setRequestor(requestor);

        ItemRequest saved = requestRepository.save(request);
        log.info("Запрос вещи создан с id={}", saved.getId());

        return ItemRequestMapper.toResponseDto(saved, Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        log.debug("Получение запросов пользователя {}", userId);

        validationUtils.getExistingUser(userId);

        return requestRepository.findByRequestorIdOrderByCreatedDesc(userId)
                .stream()
                .map(req -> {
                    List<ItemForRequestDto> items = findItemsByRequestId(req.getId());
                    return ItemRequestMapper.toResponseDto(req, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponseDto> getAllRequests(Long userId) {
        log.debug("Получение всех запросов, кроме запросов пользователя {}", userId);

        validationUtils.getExistingUser(userId);

        return requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId)
                .stream()
                .map(req -> {
                    List<ItemForRequestDto> items = findItemsByRequestId(req.getId());
                    return ItemRequestMapper.toResponseDto(req, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestResponseDto getRequestById(Long requestId, Long userId) {
        log.debug("Получение запроса с id={}", requestId);

        validationUtils.getExistingUser(userId); // проверяем, что пользователь существует

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Запрос с id #{} не найден", requestId);
                    return new NotFoundException("Запрос с таким id не найден");
                });

        List<ItemForRequestDto> items = findItemsByRequestId(requestId);
        return ItemRequestMapper.toResponseDto(request, items);
    }

    private List<ItemForRequestDto> findItemsByRequestId(Long requestId) {
        return itemRepository.findByRequestId(requestId)
                .stream()
                .map(item -> new ItemForRequestDto(
                        item.getId(),
                        item.getName(),
                        item.getOwner().getId()
                ))
                .collect(Collectors.toList());
    }
}