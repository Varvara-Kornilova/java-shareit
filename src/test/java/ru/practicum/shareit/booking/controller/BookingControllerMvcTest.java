package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_ShouldReturn200() throws Exception {
        BookingCreateDto createDto = new BookingCreateDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingResponseDto response = new BookingResponseDto(1L,
                createDto.getStart(), createDto.getEnd(),
                ru.practicum.shareit.booking.model.BookingStatus.WAITING,
                new BookingResponseDto.ItemForBookingDto(10L, "Дрель"),
                new BookingResponseDto.BookerDto(1L, "Booker"));

        when(bookingService.createBooking(eq(1L), any(BookingCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createBooking_WithInvalidDates_ShouldReturn400() throws Exception {
        BookingCreateDto invalidDto = new BookingCreateDto(10L,
                LocalDateTime.now().plusDays(2), // start
                LocalDateTime.now().plusDays(1)); // end (раньше start!)

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_Approve_ShouldReturn200() throws Exception {
        BookingResponseDto response = new BookingResponseDto(1L,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                ru.practicum.shareit.booking.model.BookingStatus.APPROVED,
                new BookingResponseDto.ItemForBookingDto(10L, "Дрель"),
                new BookingResponseDto.BookerDto(1L, "Booker"));

        when(bookingService.updateBookingStatus(eq(1L), eq(2L), eq(true))).thenReturn(response);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getAllBookingsByBooker_WithStateFilter() throws Exception {
        when(bookingService.getAllBookingsByBooker(eq(1L), eq(BookingState.FUTURE)))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}