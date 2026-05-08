package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void serializeItemDto_WithRequestId() throws Exception {
        ItemDto dto = new ItemDto(10L, "Дрель", "Профессиональная", true, null, null, List.of(), 5L);

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }

    @Test
    void serializeItemDto_WithComments() throws Exception {
        CommentDto comment = new CommentDto(1L, "Good", "User",
                java.time.LocalDateTime.of(2026, 5, 8, 12, 0, 0));
        ItemDto dto = new ItemDto(10L, "Дрель", "Профессиональная", true, null, null, List.of(comment), null);

        var result = json.write(dto);

        assertThat(result).extractingJsonPathArrayValue("$.comments").hasSize(1);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Good");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("User");
    }
}