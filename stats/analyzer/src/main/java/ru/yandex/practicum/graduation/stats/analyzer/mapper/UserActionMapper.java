package ru.yandex.practicum.graduation.stats.analyzer.mapper;

import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.graduation.stats.analyzer.model.UserInteraction;

public class UserActionMapper {
    public static UserInteraction toEntity(UserActionAvro avro) {
        double weight = switch (avro.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> 0.0;
        };
        return UserInteraction.builder()
                .userId(avro.getUserId())
                .eventId(avro.getEventId())
                .maxWeight(weight)
                .updatedAt(avro.getTimestamp().toEpochMilli())
                .build();
    }
}
