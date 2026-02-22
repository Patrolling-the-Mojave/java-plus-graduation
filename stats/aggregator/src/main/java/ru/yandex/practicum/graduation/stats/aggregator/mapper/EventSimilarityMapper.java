package ru.yandex.practicum.graduation.stats.aggregator.mapper;

import ru.practicum.stats.avro.EventSimilarityAvro;

import java.time.Instant;

public class EventSimilarityMapper {

    public static EventSimilarityAvro toSimilarity(long eventA, long eventB, double similarity, Instant timestamp) {
        return EventSimilarityAvro.newBuilder()
                .setEventA(eventA)
                .setEventB(eventB)
                .setScore(similarity)
                .setTimestamp(timestamp)
                .build();
    }
}
