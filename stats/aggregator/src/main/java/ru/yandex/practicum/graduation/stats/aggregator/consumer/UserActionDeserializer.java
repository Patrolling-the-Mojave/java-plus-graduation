package ru.yandex.practicum.graduation.stats.aggregator.consumer;

import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.avro.deserialization.BaseAvroDeserializer;

public class UserActionDeserializer extends BaseAvroDeserializer<UserActionAvro> {
    public UserActionDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
