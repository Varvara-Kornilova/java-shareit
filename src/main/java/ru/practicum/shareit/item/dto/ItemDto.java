package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200)
    private String description;

    @NotNull(message = "Статус не может быть неопределен")
    private Boolean available;

    private BookingItemDto lastBooking;
    private BookingItemDto nextBooking;

    private List<CommentDto> comments = new ArrayList<>();
}