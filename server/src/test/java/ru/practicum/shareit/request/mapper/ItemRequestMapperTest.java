package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemRequestMapperTest {

    @Test
    public void toResponseDto_WithItems() {
        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Нужен инструмент");
        request.setCreated(LocalDateTime.of(2024, 6, 1, 12, 0));

        List<ItemForRequestDto> items = List.of(
                new ItemForRequestDto(1L, "Дрель", 5L)
        );

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request, items);

        assertEquals(10L, dto.getId());
        assertEquals("Нужен инструмент", dto.getDescription());
        assertEquals(LocalDateTime.of(2024, 6, 1, 12, 0), dto.getCreated());
        assertEquals(1, dto.getItems().size());
        assertEquals("Дрель", dto.getItems().get(0).getName());
    }

    @Test
    public void toResponseDto_WithNullItems_ReturnsEmptyList() {
        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Запрос");
        request.setCreated(LocalDateTime.now());

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request, null);

        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
    }

    @Test
    public void toResponseDto_WithEmptyItemsList() {
        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Запрос");
        request.setCreated(LocalDateTime.now());

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request, List.of());

        assertTrue(dto.getItems().isEmpty());
    }
}