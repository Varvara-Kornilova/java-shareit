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
class ItemRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private final long user_id = 1L;
    private final long request_id = 10L;

    @Test
    void createRequest_Success() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");
        ItemRequestResponseDto response = new ItemRequestResponseDto(
                request_id, "Нужна дрель", LocalDateTime.now(), List.of());

        when(itemRequestService.createRequest(eq(user_id), any(ItemRequestCreateDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", user_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(request_id))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));

        verify(itemRequestService).createRequest(eq(user_id), any(ItemRequestCreateDto.class));
    }

    @Test
    void getUserRequests_Success() throws Exception {
        ItemRequestResponseDto request = new ItemRequestResponseDto(
                request_id, "Запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getUserRequests(eq(user_id)))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", user_id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(request_id));

        verify(itemRequestService).getUserRequests(eq(user_id));
    }

    @Test
    void getAllRequests_Success() throws Exception {
        ItemRequestResponseDto request = new ItemRequestResponseDto(
                request_id, "Чужой запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getAllRequests(eq(user_id)))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", user_id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Чужой запрос"));

        verify(itemRequestService).getAllRequests(eq(user_id));
    }

    @Test
    void getRequestById_Success() throws Exception {
        ItemRequestResponseDto response = new ItemRequestResponseDto(
                request_id, "Запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getRequestById(eq(request_id), eq(user_id)))
                .thenReturn(response);

        mockMvc.perform(get("/requests/{requestId}", request_id)
                        .header("X-Sharer-User-Id", user_id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request_id));

        verify(itemRequestService).getRequestById(eq(request_id), eq(user_id));
    }

    @Test
    void getRequestById_NotFound_ThrowsException() throws Exception {
        when(itemRequestService.getRequestById(eq(request_id), eq(user_id)))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/{requestId}", request_id)
                        .header("X-Sharer-User-Id", user_id))
                .andExpect(status().isNotFound());
    }
}