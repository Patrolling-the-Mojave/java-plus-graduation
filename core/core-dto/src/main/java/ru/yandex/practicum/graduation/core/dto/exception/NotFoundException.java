package ru.yandex.practicum.graduation.core.dto.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
