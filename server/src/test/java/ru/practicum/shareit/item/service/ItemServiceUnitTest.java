package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ValidationUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ValidationUtils validationUtils;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void search_NullText_ReturnsEmptyList() {
        var result = itemService.search(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_BlankText_ReturnsEmptyList() {
        var result = itemService.search("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    void updateItem_PartialUpdate_OnlyName() {
        User owner = new User();
        owner.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setName("Старое имя");
        item.setDescription("Старое описание");
        item.setAvailable(true);
        item.setOwner(owner);

        ItemUpdateDto updateDto = new ItemUpdateDto(10L, "Новое имя", null, null);

        when(validationUtils.getExistingUser(1L)).thenReturn(owner);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = itemService.updateItem(1L, 10L, updateDto);

        assertEquals("Новое имя", result.getName());
        assertEquals("Старое описание", result.getDescription());
        assertTrue(result.getAvailable());
        verify(itemRepository).save(item);
    }

    @Test
    void deleteItem_NotOwner_ThrowsException() {
        User owner = new User();
        owner.setId(2L);
        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        when(validationUtils.getExistingUser(1L)).thenReturn(new User());
        when(validationUtils.getExistingItem(10L)).thenReturn(item);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.deleteItem(1L, 10L));
        assertTrue(ex.getMessage().contains("не принадлежит"));
    }
}