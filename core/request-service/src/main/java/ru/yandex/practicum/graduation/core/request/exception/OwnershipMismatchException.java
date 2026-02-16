package ru.yandex.practicum.graduation.core.request.exception;

public class OwnershipMismatchException extends RuntimeException {
    public OwnershipMismatchException(String message) {
        super(message);
    }
}
