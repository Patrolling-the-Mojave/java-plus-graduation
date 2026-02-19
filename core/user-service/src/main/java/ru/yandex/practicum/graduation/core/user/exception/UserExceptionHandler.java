package ru.yandex.practicum.graduation.core.user.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.graduation.core.dto.exception.ApiError;
import ru.yandex.practicum.graduation.core.dto.exception.ConflictException;
import ru.yandex.practicum.graduation.core.dto.exception.ValidationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class UserExceptionHandler {

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .collect(Collectors.toList());

        errors.addAll(getStackTraceAsList(e));

        log.warn("Ошибка валидации: {}", errors);
        return new ApiError(
                errors,
                "Некорректно составленный запрос",
                "Ошибка валидации параметров",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(ValidationException e) {
        log.warn("ValidationException: {}", e.getMessage(), e);
        return new ApiError(
                getStackTraceAsList(e),
                e.getMessage(),
                "Некорректно составленный запрос.",
                HttpStatus.BAD_REQUEST
        );
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
