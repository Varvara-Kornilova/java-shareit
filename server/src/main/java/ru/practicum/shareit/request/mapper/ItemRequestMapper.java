package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Collections;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

public class ItemRequestMapper {

    public static ItemRequestResponseDto toResponseDto(ItemRequest request, List<ItemForRequestDto> items) {
        return new ItemRequestResponseDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items != null ? items : Collections.emptyList()
        );
    }
}