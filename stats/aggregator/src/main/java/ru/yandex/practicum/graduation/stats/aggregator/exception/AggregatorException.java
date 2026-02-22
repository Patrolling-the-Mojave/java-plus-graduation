package ru.yandex.practicum.graduation.stats.aggregator.exception;

public class AggregatorException extends RuntimeException {
    public AggregatorException(String message) {
        super(message);
    }

    public AggregatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
