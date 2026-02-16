package ru.yandex.practicum.graduation.core.dto;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
