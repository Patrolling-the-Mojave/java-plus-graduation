package ru.yandex.practicum.graduation.core.interaction;

public class EventClientException extends RuntimeException {
    public EventClientException(String message) {
        super(message);
    }

    public EventClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
