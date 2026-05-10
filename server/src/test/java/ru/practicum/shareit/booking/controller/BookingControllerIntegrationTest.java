package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private final long user_id = 1L;
    private final long booking_id = 10L;
    private final long item_id = 100L;

    @Test
    void createBooking_Success() throws Exception {
        BookingCreateDto request = new BookingCreateDto(item_id,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        BookingResponseDto response = new BookingResponseDto(booking_id, request.getStart(), request.getEnd(),
                BookingStatus.WAITING,
                new BookingResponseDto.ItemForBookingDto(item_id, "Дрель"),
                new BookingResponseDto.BookerDto(user_id, "User"));

        when(bookingService.createBooking(eq(user_id), any(BookingCreateDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", user_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking_id))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).createBooking(eq(user_id), any(BookingCreateDto.class));
    }

    @Test
    void updateBookingStatus_Approve_Success() throws Exception {
        BookingResponseDto response = new BookingResponseDto(booking_id, null, null,
                BookingStatus.APPROVED, null, null);

        when(bookingService.updateBookingStatus(eq(booking_id), eq(user_id), eq(true)))
                .thenReturn(response);

        mockMvc.perform(patch("/bookings/{bookingId}", booking_id)
                        .header("X-Sharer-User-Id", user_id)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void updateBookingStatus_Reject_Success() throws Exception {
        BookingResponseDto response = new BookingResponseDto(booking_id, null, null,
                BookingStatus.REJECTED, null, null);

        when(bookingService.updateBookingStatus(eq(booking_id), eq(user_id), eq(false)))
                .thenReturn(response);

        mockMvc.perform(patch("/bookings/{bookingId}", booking_id)
                        .header("X-Sharer-User-Id", user_id)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void getBooking_AsBooker_Success() throws Exception {
        BookingResponseDto response = new BookingResponseDto(booking_id, null, null,
                BookingStatus.WAITING, null, new BookingResponseDto.BookerDto(user_id, "Booker"));

        when(bookingService.getBooking(eq(user_id), eq(booking_id)))
                .thenReturn(response);

        mockMvc.perform(get("/bookings/{bookingId}", booking_id)
                        .header("X-Sharer-User-Id", user_id))
                .andExpect(status().isOk());
    }

    @Test
    void getBooking_AccessDenied_ThrowsException() throws Exception {
        when(bookingService.getBooking(eq(user_id), eq(booking_id)))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvc.perform(get("/bookings/{bookingId}", booking_id)
                        .header("X-Sharer-User-Id", user_id))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllBookingsByBooker_WithStateFilter() throws Exception {
        when(bookingService.getAllBookingsByBooker(eq(user_id), eq(BookingState.FUTURE), eq(0), eq(10)))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", user_id)
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBookingsByBooker_InvalidState_ThrowsException() throws Exception {
        // Контроллер сам бросает IllegalArgumentException → 400 Bad Request
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", user_id)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllBookingsByOwner_WithAllStates() throws Exception {
        for (BookingState state : BookingState.values()) {
            when(bookingService.getAllBookingsByOwner(eq(user_id), eq(state), eq(0), eq(10)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/bookings/owner")
                            .header("X-Sharer-User-Id", user_id)
                            .param("state", state.name()))
                    .andExpect(status().isOk());
        }
    }
}