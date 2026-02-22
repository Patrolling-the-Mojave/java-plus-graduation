package ru.yandex.practicum.avro.deserialization;

public class DeserializationException extends RuntimeException{
    public DeserializationException() {
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }

    public DeserializationException(String message) {
        super(message);
    }
}
