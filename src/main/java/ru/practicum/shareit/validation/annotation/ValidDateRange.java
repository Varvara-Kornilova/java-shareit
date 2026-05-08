package ru.practicum.shareit.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.shareit.validation.validator.DateRangeValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "Дата начала должна быть раньше даты окончания";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}