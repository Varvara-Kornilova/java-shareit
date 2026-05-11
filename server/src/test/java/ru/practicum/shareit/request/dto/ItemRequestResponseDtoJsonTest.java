package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.config.EmptyJsonConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = EmptyJsonConfig.class)
public class ItemRequestResponseDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

    @Test
    public void serializeDto_ShouldMapToJsonCorrectly() throws Exception {
        ItemForRequestDto item = new ItemForRequestDto(100L, "Дрель", 1L);
        ItemRequestResponseDto dto = new ItemRequestResponseDto(
                1L,
                "Нужен шуруповерт",
                LocalDateTime.of(2024, 5, 12, 10, 30),
                List.of(item)
        );

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Нужен шуруповерт");

        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2024-05-12T10:30:00");

        assertThat(result).hasJsonPathArrayValue("$.items");
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
    }

    @Test
    public void serializeDto_WithEmptyItems_ShouldIncludeEmptyArray() throws Exception {
        ItemRequestResponseDto dto = new ItemRequestResponseDto(
                2L, "Другой запрос", LocalDateTime.now(), List.of()
        );

        var result = json.write(dto);

        assertThat(result).hasJsonPathArrayValue("$.items");
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }
}