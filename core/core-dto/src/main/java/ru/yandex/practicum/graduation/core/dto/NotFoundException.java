package ru.yandex.practicum.graduation.core.dto;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
