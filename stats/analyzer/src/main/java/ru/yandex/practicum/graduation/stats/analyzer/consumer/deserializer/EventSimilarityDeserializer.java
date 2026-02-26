package ru.yandex.practicum.graduation.stats.analyzer.consumer.deserializer;

import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.avro.deserialization.BaseAvroDeserializer;

public class EventSimilarityDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {
    public EventSimilarityDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
