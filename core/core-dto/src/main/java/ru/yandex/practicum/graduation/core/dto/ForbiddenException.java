package ru.yandex.practicum.graduation.core.dto;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
