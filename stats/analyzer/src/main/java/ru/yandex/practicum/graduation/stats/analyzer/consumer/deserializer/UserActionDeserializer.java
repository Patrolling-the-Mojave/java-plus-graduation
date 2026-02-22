package ru.yandex.practicum.graduation.stats.analyzer.consumer.deserializer;

import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.avro.deserialization.BaseAvroDeserializer;

public class UserActionDeserializer extends BaseAvroDeserializer<UserActionAvro> {
    public UserActionDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
