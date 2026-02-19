package ru.yandex.practicum.graduation.core.dto.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
