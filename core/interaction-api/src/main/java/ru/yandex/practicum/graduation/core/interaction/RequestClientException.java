package ru.yandex.practicum.graduation.core.interaction;

public class RequestClientException extends RuntimeException {
    public RequestClientException(String message) {
        super(message);
    }

  public RequestClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
