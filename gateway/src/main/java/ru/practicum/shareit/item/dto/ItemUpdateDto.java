package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemUpdateDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Size(max = 100)
    private String name;

    @Size(max = 200)
    private String description;

    private Boolean available;
}