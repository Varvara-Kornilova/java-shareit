package ru.practicum.shareit.validation;

public interface EntityValidator<T, D> {

    void validate(T entity, D dto);

    boolean supports(Class<?> entityType);
}