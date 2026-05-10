package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleNotFound_ReturnsCorrectMap() {
        NotFoundException ex = new NotFoundException("Not found");
        Map<String, String> result = errorHandler.handleNotFound(ex);
        assertEquals("Not found", result.get("error"));
    }

    @Test
    void handleAccessDenied_ReturnsCorrectMap() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        Map<String, String> result = errorHandler.handleAccessDenied(ex);
        assertEquals("Access denied", result.get("error"));
    }

    @Test
    void handleConflict_ReturnsCorrectMap() {
        DuplicatedDataException ex = new DuplicatedDataException("Email exists");
        Map<String, String> result = errorHandler.handleConflict(ex);
        assertEquals("Email exists", result.get("error"));
    }

    @Test
    void handleValidation_MethodArgumentNotValidException_WithMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = mock(FieldError.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        when(fieldError.getDefaultMessage()).thenReturn("Field is required");

        Map<String, String> result = errorHandler.handleValidation(ex);
        assertEquals("Field is required", result.get("error"));
    }

    @Test
    void handleValidation_MethodArgumentNotValidException_EmptyErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());  // ← Пустой список, без матчеров!

        Map<String, String> result = errorHandler.handleValidation(ex);
        assertEquals("Validation failed", result.get("error"));
    }

    @Test
    void handleValidation_ValidationException_ReturnsCorrectMap() {
        ValidationException ex = new ValidationException("Invalid date range");
        Map<String, String> result = errorHandler.handleValidation(ex);
        assertEquals("Invalid date range", result.get("error"));
    }
}