package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void addItem_WithValidRequestId_Success() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto requester = userService.addUser(new UserDto(null, "Requester", "req@test.com"));

        var request = requestService.createRequest(requester.getId(),
                new ItemRequestCreateDto("Нужна дрель"));

        ItemDto newItem = new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), request.getId());

        ItemDto created = itemService.addItem(owner.getId(), newItem);

        assertNotNull(created.getId());
        assertEquals("Дрель", created.getName());
        assertEquals(request.getId(), created.getRequestId());
    }

    @Test
    void addItem_WithInvalidRequestId_ThrowsException() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        ItemDto newItem = new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), 999L);

        assertThrows(NotFoundException.class, () ->
                itemService.addItem(owner.getId(), newItem));
    }

    @Test
    void addComment_AfterApprovedBooking_Success() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto booker = userService.addUser(new UserDto(null, "Booker", "booker@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        Booking booking = new Booking();
        booking.setBooker(userRepository.findById(booker.getId()).orElseThrow());
        booking.setItem(itemRepository.findById(item.getId()).orElseThrow());
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentCreateDto commentDto = new CommentCreateDto("Отличная вещь!");

        var comment = itemService.addComment(booker.getId(), item.getId(), commentDto);

        assertNotNull(comment.getId());
        assertEquals("Отличная вещь!", comment.getText());
        assertEquals(booker.getName(), comment.getAuthorName());
    }

    @Test
    void addComment_WithoutApprovedBooking_ThrowsException() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        UserDto other = userService.addUser(new UserDto(null, "Other", "other@test.com"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        CommentCreateDto commentDto = new CommentCreateDto("Отличная вещь!");

        assertThrows(ValidationException.class, () ->
                itemService.addComment(other.getId(), item.getId(), commentDto));
    }

    @Test
    void getItemById_WithComments_ReturnsComments() {
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));
        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null));

        ItemDto found = itemService.getItem(item.getId());

        assertNotNull(found);
        assertEquals("Дрель", found.getName());
        assertNotNull(found.getComments());
    }
}