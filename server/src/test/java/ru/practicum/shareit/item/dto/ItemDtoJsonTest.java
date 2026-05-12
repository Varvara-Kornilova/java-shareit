package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.config.EmptyJsonConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = EmptyJsonConfig.class)
public class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    public void serializeItemDto_ShouldMapToJsonCorrectly() throws Exception {
        BookingItemDto lastBooking = new BookingItemDto();
        lastBooking.setId(10L);
        lastBooking.setBookerId(1L);
        lastBooking.setStart(LocalDateTime.of(2024, 5, 1, 10, 0));
        lastBooking.setEnd(LocalDateTime.of(2024, 5, 5, 18, 0));

        BookingItemDto nextBooking = new BookingItemDto();
        nextBooking.setId(20L);
        nextBooking.setBookerId(2L);
        nextBooking.setStart(LocalDateTime.of(2024, 6, 1, 10, 0));
        nextBooking.setEnd(LocalDateTime.of(2024, 6, 5, 18, 0));

        CommentDto comment = new CommentDto();
        comment.setId(100L);
        comment.setText("Отличный товар!");
        comment.setAuthorName("Иван");

        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Дрель");
        dto.setDescription("Мощная дрель для любых задач");
        dto.setAvailable(true);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(List.of(comment));
        dto.setRequestId(5L);

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");

        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Мощная дрель для любых задач");

        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();

        assertThat(result).hasJsonPathNumberValue("$.lastBooking.id");
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(1);

        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start")
                .isEqualTo("2024-05-01T10:00:00");

        assertThat(result).hasJsonPathArrayValue("$.comments");
        assertThat(result).extractingJsonPathArrayValue("$.comments").hasSize(1);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text")
                .isEqualTo("Отличный товар!");

        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }

    @Test
    public void deserializeItemDto_ShouldMapFromJsonCorrectly() throws Exception {
        String jsonContent =
                "{\"id\": 1, \"name\": \"Дрель\", \"description\": \"Мощная дрель\", " +
                        "\"available\": true, \"lastBooking\": null, \"nextBooking\": null, " +
                        "\"comments\": [], \"requestId\": 5}";

        ItemDto dto = json.parseObject(jsonContent);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
        assertThat(dto.getComments()).isEmpty();
        assertThat(dto.getRequestId()).isEqualTo(5L);
    }
}