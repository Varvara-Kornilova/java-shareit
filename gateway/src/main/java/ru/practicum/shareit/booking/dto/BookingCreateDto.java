package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.validation.annotation.ValidDateRange;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange
public class BookingCreateDto {

    @NotNull(message = "ID вещи не может быть пустым")
    private Long itemId;

    @NotNull(message = "Дата начала не может быть пустой")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания не может быть пустой")
    private LocalDateTime end;
}