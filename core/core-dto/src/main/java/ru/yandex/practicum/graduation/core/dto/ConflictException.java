package ru.yandex.practicum.graduation.core.dto;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
