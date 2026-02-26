package ru.yandex.practicum.graduation.stats.analyzer.mapper;

import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.graduation.stats.analyzer.model.EventSimilarity;

public class EventSimilarityMapper {

    public static EventSimilarity toEntity(EventSimilarityAvro avro){
        long eventA = Math.min(avro.getEventA(), avro.getEventB());
        long eventB = Math.max(avro.getEventA(), avro.getEventB());
        return EventSimilarity.builder()
                .eventA(eventA)
                .eventB(eventB)
                .similarityScore(avro.getScore())
                .updatedAt(avro.getTimestamp().toEpochMilli())
                .build();
    }
}
