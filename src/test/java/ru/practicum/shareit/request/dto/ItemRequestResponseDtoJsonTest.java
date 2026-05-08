package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestResponseDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

    @Test
    void serializeRequestDto() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 5, 8, 12, 0, 0);
        ItemRequestResponseDto dto = new ItemRequestResponseDto(10L, "Need drill", now, List.of());

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need drill");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-05-08T12:00:00");
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }
}