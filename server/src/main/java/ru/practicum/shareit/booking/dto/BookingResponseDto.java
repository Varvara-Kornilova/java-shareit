package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private ItemForBookingDto item;
    private BookerDto booker;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemForBookingDto {
        private Long id;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookerDto {
        private Long id;
        private String name;
    }
}