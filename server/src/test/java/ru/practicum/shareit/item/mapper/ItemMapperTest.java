package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import static org.junit.jupiter.api.Assertions.*;

public class ItemMapperTest {

    @Test
    public void toItemDto_MapsAllFields() {
        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setDescription("Профессиональная");
        item.setAvailable(true);
        item.setRequestId(5L);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(10L, dto.getId());
        assertEquals("Дрель", dto.getName());
        assertEquals("Профессиональная", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(5L, dto.getRequestId());
        assertNull(dto.getLastBooking()); // не маппится в toItemDto
    }

    @Test
    public void toItem_MapsFromItemDto() {
        ItemDto dto = new ItemDto(10L, "Дрель", "Описание", false, null, null, null, 5L);

        Item item = ItemMapper.toItem(dto);

        assertEquals(10L, item.getId());
        assertEquals("Дрель", item.getName());
        assertEquals("Описание", item.getDescription());
        assertFalse(item.getAvailable());
        assertEquals(5L, item.getRequestId());
    }

    @Test
    public void toItemUpdateDto_MapsFields() {
        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);

        ItemUpdateDto dto = ItemMapper.toItemUpdateDto(item);

        assertEquals(10L, dto.getId());
        assertEquals("Дрель", dto.getName());
        assertEquals("Описание", dto.getDescription());
        assertTrue(dto.getAvailable());
    }

    @Test
    public void toItem_FromItemUpdateDto() {
        ItemUpdateDto dto = new ItemUpdateDto(10L, "Новое имя", "Новое описание", false);

        Item item = ItemMapper.toItem(dto);

        assertEquals(10L, item.getId());
        assertEquals("Новое имя", item.getName());
        assertEquals("Новое описание", item.getDescription());
        assertFalse(item.getAvailable());
    }
}