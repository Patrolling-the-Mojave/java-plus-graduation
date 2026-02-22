package ru.yandex.practicum.graduation.stats.analyzer.exception;

public class AnalyzerServiceException extends RuntimeException{
    public AnalyzerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
