package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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