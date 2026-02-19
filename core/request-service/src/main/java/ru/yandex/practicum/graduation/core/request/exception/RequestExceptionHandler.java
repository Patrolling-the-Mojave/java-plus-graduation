package ru.yandex.practicum.graduation.core.request.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.graduation.core.dto.exception.ApiError;
import ru.yandex.practicum.graduation.core.dto.exception.ConflictException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class RequestExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(ConflictException e) {
        log.warn("ConflictException: {}", e.getMessage(), e);
        return new ApiError(
                getStackTraceAsList(e),
                e.getMessage(),
                "Нарушение целостности данных.",
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(final IllegalArgumentException exception) {
        log.warn("illegal argument", exception);
        return new ApiError(getStackTraceAsList(exception),
                exception.getMessage(),
                "передан неверный аргумент",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAllExceptions(Exception e) {
        log.error("Internal server error: ", e);
        return new ApiError(
                getStackTraceAsList(e),
                "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private List<String> getStackTraceAsList(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        return List.of(stackTrace.split("\n"))
                .stream()
                .limit(20)
                .toList();
    }
}
