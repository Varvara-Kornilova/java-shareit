package ru.practicum.shareit.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.validation.annotation.ValidDateRange;

import java.time.LocalDateTime;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, BookingCreateDto> {

    @Override
    public boolean isValid(BookingCreateDto dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        LocalDateTime start = dto.getStart();
        LocalDateTime end = dto.getEnd();

        if (start == null || end == null) return true; // @NotNull обработает отдельно

        boolean isValid = start.isBefore(end);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Дата начала должна быть раньше даты окончания")
                    .addPropertyNode("start")
                    .addConstraintViolation();
        }
        return isValid;
    }
}