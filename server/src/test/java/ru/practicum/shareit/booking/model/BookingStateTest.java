package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BookingStateTest {

    @ParameterizedTest
    @CsvSource({
            "ALL, ALL",
            "all, ALL",
            "CURRENT, CURRENT",
            "past, PAST",
            "FUTURE, FUTURE",
            "waiting, WAITING",
            "REJECTED, REJECTED"
    })
    public void from_ValidString_ReturnsState(String input, BookingState expected) {
        Optional<BookingState> result = BookingState.from(input);
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    public void from_InvalidString_ReturnsEmpty() {
        Optional<BookingState> result = BookingState.from("INVALID");
        assertFalse(result.isPresent());
    }

    @Test
    public void from_Null_ReturnsEmpty() {
        Optional<BookingState> result = BookingState.from(null);
        assertFalse(result.isPresent());
    }
}