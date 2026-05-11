package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ValidationUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceUnitTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ValidationUtils validationUtils;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    public void getRequestById_NotFound_ThrowsException() {
        User user = new User();
        user.setId(1L);

        when(validationUtils.getExistingUser(1L)).thenReturn(user);
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(999L, 1L));
        assertTrue(ex.getMessage().contains("не найден"));
    }

    @Test
    public void findItemsByRequestId_EmptyList_ReturnsEmpty() {
        User user = new User();
        user.setId(1L);
        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Запрос");
        request.setCreated(LocalDateTime.now());
        request.setRequestor(user);

        when(validationUtils.getExistingUser(1L)).thenReturn(user);
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(10L)).thenReturn(List.of());
        ItemRequestResponseDto result = itemRequestService.getRequestById(10L, 1L);

        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    public void findItemsByRequestId_WithItems_MapsCorrectly() {
        User user = new User();
        user.setId(1L);
        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Запрос");
        request.setCreated(LocalDateTime.now());
        request.setRequestor(user);

        Item item = new Item();
        item.setId(100L);
        item.setName("Дрель");
        User itemOwner = new User();
        itemOwner.setId(5L);
        item.setOwner(itemOwner);

        when(validationUtils.getExistingUser(1L)).thenReturn(user);
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));

        ItemRequestResponseDto result = itemRequestService.getRequestById(10L, 1L);

        assertEquals(1, result.getItems().size());
        assertEquals("Дрель", result.getItems().get(0).getName());
        assertEquals(5L, result.getItems().get(0).getOwnerId());
    }
}