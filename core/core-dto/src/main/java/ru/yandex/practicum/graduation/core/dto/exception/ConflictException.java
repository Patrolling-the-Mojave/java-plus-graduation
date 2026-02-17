package ru.yandex.practicum.graduation.core.dto.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
