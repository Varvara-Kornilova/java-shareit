package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String text;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String authorName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime created;
}