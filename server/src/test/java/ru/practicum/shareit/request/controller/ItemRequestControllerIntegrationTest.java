package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.*;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ItemRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private final long userId = 1L;
    private final long requestId = 10L;

    @Test
    public void createRequest_Success() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");
        ItemRequestResponseDto response = new ItemRequestResponseDto(
                requestId, "Нужна дрель", LocalDateTime.now(), List.of());

        when(itemRequestService.createRequest(eq(userId), any(ItemRequestCreateDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));

        verify(itemRequestService).createRequest(eq(userId), any(ItemRequestCreateDto.class));
    }

    @Test
    public void getUserRequests_Success() throws Exception {
        ItemRequestResponseDto request = new ItemRequestResponseDto(
                requestId, "Запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getUserRequests(eq(userId)))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId));

        verify(itemRequestService).getUserRequests(eq(userId));
    }

    @Test
    public void getAllRequests_Success() throws Exception {
        ItemRequestResponseDto request = new ItemRequestResponseDto(
                requestId, "Чужой запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getAllRequests(eq(userId)))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Чужой запрос"));

        verify(itemRequestService).getAllRequests(eq(userId));
    }

    @Test
    public void getRequestById_Success() throws Exception {
        ItemRequestResponseDto response = new ItemRequestResponseDto(
                requestId, "Запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getRequestById(eq(requestId), eq(userId)))
                .thenReturn(response);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId));

        verify(itemRequestService).getRequestById(eq(requestId), eq(userId));
    }

    @Test
    public void getRequestById_NotFound_ThrowsException() throws Exception {
        when(itemRequestService.getRequestById(eq(requestId), eq(userId)))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }
}