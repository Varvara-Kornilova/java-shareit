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

    private final long USER_ID = 1L;
    private final long REQUEST_ID = 10L;

    @Test
    void createRequest_Success() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");
        ItemRequestResponseDto response = new ItemRequestResponseDto(
                REQUEST_ID, "Нужна дрель", LocalDateTime.now(), List.of());

        when(itemRequestService.createRequest(eq(USER_ID), any(ItemRequestCreateDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));

        verify(itemRequestService).createRequest(eq(USER_ID), any(ItemRequestCreateDto.class));
    }

    @Test
    void getUserRequests_Success() throws Exception {
        ItemRequestResponseDto request = new ItemRequestResponseDto(
                REQUEST_ID, "Запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getUserRequests(eq(USER_ID)))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(REQUEST_ID));

        verify(itemRequestService).getUserRequests(eq(USER_ID));
    }

    @Test
    void getAllRequests_Success() throws Exception {
        ItemRequestResponseDto request = new ItemRequestResponseDto(
                REQUEST_ID, "Чужой запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getAllRequests(eq(USER_ID)))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Чужой запрос"));

        verify(itemRequestService).getAllRequests(eq(USER_ID));
    }

    @Test
    void getRequestById_Success() throws Exception {
        ItemRequestResponseDto response = new ItemRequestResponseDto(
                REQUEST_ID, "Запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getRequestById(eq(REQUEST_ID), eq(USER_ID)))
                .thenReturn(response);

        mockMvc.perform(get("/requests/{requestId}", REQUEST_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID));

        verify(itemRequestService).getRequestById(eq(REQUEST_ID), eq(USER_ID));
    }

    @Test
    void getRequestById_NotFound_ThrowsException() throws Exception {
        when(itemRequestService.getRequestById(eq(REQUEST_ID), eq(USER_ID)))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/{requestId}", REQUEST_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isNotFound());
    }
}