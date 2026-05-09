package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestResponseDtoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void serializeRequestDto() throws Exception {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(1L);
        dto.setDescription("Нужен шуруповерт");
        dto.setCreated(LocalDateTime.now());
        dto.setItems(Collections.emptyList());

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Нужен шуруповерт\"");
        assertThat(json).contains("\"created\"");
    }
}