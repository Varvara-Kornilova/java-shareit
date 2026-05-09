package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Test
    void createRequest_Success() {
        UserDto user = userService.addUser(new UserDto(null, "Test User", "test@test.com"));
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");

        ItemRequestResponseDto response = requestService.createRequest(user.getId(), createDto);

        assertNotNull(response.getId());
        assertEquals("Нужна дрель", response.getDescription());
        assertNotNull(response.getCreated());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void getUserRequests_WithAndWithoutRequests() {
        UserDto user1 = userService.addUser(new UserDto(null, "User1", "user1@test.com"));
        UserDto user2 = userService.addUser(new UserDto(null, "User2", "user2@test.com"));

        requestService.createRequest(user1.getId(), new ItemRequestCreateDto("Request 1"));
        requestService.createRequest(user1.getId(), new ItemRequestCreateDto("Request 2"));

        var user1Requests = requestService.getUserRequests(user1.getId());
        var user2Requests = requestService.getUserRequests(user2.getId());

        assertEquals(2, user1Requests.size());
        assertEquals(0, user2Requests.size());
    }

    @Test
    void getAllRequests_ExcludesOwnRequests() {
        UserDto user1 = userService.addUser(new UserDto(null, "User1", "user1@test.com"));
        UserDto user2 = userService.addUser(new UserDto(null, "User2", "user2@test.com"));

        requestService.createRequest(user1.getId(), new ItemRequestCreateDto("Req from 1"));
        requestService.createRequest(user2.getId(), new ItemRequestCreateDto("Req from 2"));

        var allForUser1 = requestService.getAllRequests(user1.getId());
        assertEquals(1, allForUser1.size());
        assertEquals("Req from 2", allForUser1.get(0).getDescription());
    }
}