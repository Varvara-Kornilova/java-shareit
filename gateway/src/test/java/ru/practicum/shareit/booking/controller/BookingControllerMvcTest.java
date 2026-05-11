package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.client.BookingClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createBooking_ShouldReturn200() throws Exception {
        BookingCreateDto createDto = new BookingCreateDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        BookingResponseDto response = new BookingResponseDto(1L,
                createDto.getStart(), createDto.getEnd(),
                BookingStatus.WAITING,
                new BookingResponseDto.ItemForBookingDto(10L, "Дрель"),
                new BookingResponseDto.BookerDto(1L, "Booker"));

        when(bookingClient.bookItem(eq(1L), any(BookingCreateDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    public void createBooking_WithInvalidDates_ShouldReturn400() throws Exception {
        BookingCreateDto invalidDto = new BookingCreateDto(10L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateBookingStatus_Approve_ShouldReturn200() throws Exception {
        BookingResponseDto response = new BookingResponseDto(1L,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                BookingStatus.APPROVED,
                new BookingResponseDto.ItemForBookingDto(10L, "Дрель"),
                new BookingResponseDto.BookerDto(1L, "Booker"));

        when(bookingClient.updateBookingStatus(eq(2L), eq(1L), eq(true)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void getAllBookingsByBooker_WithStateFilter() throws Exception {
        when(bookingClient.getBookings(eq(1L), eq(BookingState.FUTURE), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}