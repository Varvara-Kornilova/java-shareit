package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ValidationUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private BookingRepository bookingRepository;

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
    void getAllItems_EmptyList_ReturnsEmpty() {
        User user = new User();
        user.setId(1L);

        when(validationUtils.getExistingUser(1L)).thenReturn(user);
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of());

        var result = itemService.getAllItems(1L);

        assertTrue(result.isEmpty());
        verify(itemRepository).findByOwnerId(1L);
    }

    @Test
    void getItem_NotFound_ThrowsException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.getItem(999L));
        assertTrue(ex.getMessage().contains("не найдена"));
    }

    @Test
    void addItem_WithNullRequestId_Success() {
        User owner = new User();
        owner.setId(1L);
        ItemDto newItem = new ItemDto(null, "Дрель", "Описание", true, null, null, List.of(), null);
        Item savedItem = new Item();
        savedItem.setId(10L);
        savedItem.setName("Дрель");
        savedItem.setOwner(owner);

        when(validationUtils.getExistingUser(1L)).thenReturn(owner);
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        var result = itemService.addItem(1L, newItem);

        assertNotNull(result.getId());
        assertEquals("Дрель", result.getName());
        verify(requestRepository, never()).findById(anyLong()); // не вызывался, т.к. requestId = null
    }

    @Test
    void updateItem_PartialUpdate_OnlyDescription() {
        User owner = new User();
        owner.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setName("Старое имя");
        item.setDescription("Старое описание");
        item.setAvailable(true);
        item.setOwner(owner);

        ItemUpdateDto updateDto = new ItemUpdateDto(10L, null, "Новое описание", null);

        when(validationUtils.getExistingUser(1L)).thenReturn(owner);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = itemService.updateItem(1L, 10L, updateDto);

        assertEquals("Старое имя", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_PartialUpdate_OnlyAvailable() {
        User owner = new User();
        owner.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setName("Имя");
        item.setDescription("Описание");
        item.setAvailable(true);
        item.setOwner(owner);

        ItemUpdateDto updateDto = new ItemUpdateDto(10L, null, null, false);

        when(validationUtils.getExistingUser(1L)).thenReturn(owner);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = itemService.updateItem(1L, 10L, updateDto);

        assertEquals("Имя", result.getName());
        assertEquals("Описание", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void deleteItem_Success() {
        User owner = new User();
        owner.setId(1L);
        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        when(validationUtils.getExistingUser(1L)).thenReturn(owner);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);

        assertDoesNotThrow(() -> itemService.deleteItem(1L, 10L));
        verify(itemRepository).deleteById(10L);
    }

    @Test
    void getComments_EmptyList_ReturnsEmpty() {
        Item item = new Item();
        item.setId(10L);

        when(validationUtils.getExistingItem(10L)).thenReturn(item);
        when(commentRepository.findByItemIdOrderByCreatedDesc(10L)).thenReturn(List.of());

        var result = itemService.getComments(10L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getComments_WithComments_ReturnsMapped() {
        Item item = new Item();
        item.setId(10L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Коммент");
        comment.setCreated(LocalDateTime.now());
        User author = new User();
        author.setName("Автор");
        comment.setAuthor(author);
        comment.setItem(item);

        when(validationUtils.getExistingItem(10L)).thenReturn(item);
        when(commentRepository.findByItemIdOrderByCreatedDesc(10L)).thenReturn(List.of(comment));

        var result = itemService.getComments(10L);

        assertEquals(1, result.size());
        assertEquals("Коммент", result.get(0).getText());
        assertEquals("Автор", result.get(0).getAuthorName());
    }

    @Test
    void getAllItems_WithBookings_SetsLastAndNextBooking() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setOwner(owner);

        Booking lastBooking = new Booking();
        lastBooking.setId(100L);
        lastBooking.setBooker(new User());
        lastBooking.getBooker().setId(2L);
        lastBooking.setStart(LocalDateTime.now().minusDays(10));
        lastBooking.setEnd(LocalDateTime.now().minusDays(5));

        Booking nextBooking = new Booking();
        nextBooking.setId(101L);
        nextBooking.setBooker(new User());
        nextBooking.getBooker().setId(3L);
        nextBooking.setStart(LocalDateTime.now().plusDays(5));
        nextBooking.setEnd(LocalDateTime.now().plusDays(10));

        when(validationUtils.getExistingUser(1L)).thenReturn(owner);
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(item));
        when(bookingRepository.findLastBookingByItemIdAndStatusBeforeNow(
                eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findNextBookingByItemIdAndStatusAfterNow(
                eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(nextBooking));

        var result = itemService.getAllItems(1L);

        assertFalse(result.isEmpty());
        ItemDto dto = result.iterator().next();
        assertNotNull(dto.getLastBooking());
        assertEquals(100L, dto.getLastBooking().getId());
        assertNotNull(dto.getNextBooking());
        assertEquals(101L, dto.getNextBooking().getId());
    }

    @Test
    void getAllItems_WithoutBookings_SetsNullBookings() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setOwner(owner);

        when(validationUtils.getExistingUser(1L)).thenReturn(owner);
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(item));
        when(bookingRepository.findLastBookingByItemIdAndStatusBeforeNow(
                eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());
        when(bookingRepository.findNextBookingByItemIdAndStatusAfterNow(
                eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());

        var result = itemService.getAllItems(1L);

        assertFalse(result.isEmpty());
        ItemDto dto = result.iterator().next();
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
    }


    @Test
    void search_WithText_ReturnsMappedItems() {
        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setDescription("Профессиональная");
        item.setAvailable(true);

        when(itemRepository.searchByText("дрель")).thenReturn(List.of(item));

        var result = itemService.search("дрель");

        assertFalse(result.isEmpty());
        ItemDto dto = result.iterator().next();
        assertEquals("Дрель", dto.getName());
        verify(itemRepository).searchByText("дрель");
    }


    @Test
    void updateItem_NotOwner_ThrowsException() {
        User owner = new User();
        owner.setId(2L);

        User requester = new User();
        requester.setId(1L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        ItemUpdateDto updateDto = new ItemUpdateDto(10L, "Новое имя", null, null);

        when(validationUtils.getExistingUser(1L)).thenReturn(requester);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(1L, 10L, updateDto));
        assertTrue(ex.getMessage().contains("не принадлежит"));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void deleteItem_NotOwner_ThrowsException() {
        User owner = new User();
        owner.setId(2L);

        User requester = new User();
        requester.setId(1L);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        when(validationUtils.getExistingUser(1L)).thenReturn(requester);
        when(validationUtils.getExistingItem(10L)).thenReturn(item);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.deleteItem(1L, 10L));
        assertTrue(ex.getMessage().contains("не принадлежит"));
        verify(itemRepository, never()).deleteById(anyLong());
    }
}