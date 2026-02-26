package ru.yandex.practicum.graduation.stats.aggregator.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventSimilarityService {
    private final Map<Long, Map<Long, Double>> userWeights = new ConcurrentHashMap<>();

    private final Map<Long, Double> eventSums = new ConcurrentHashMap<>();

    private final Map<Long, Map<Long, Double>> minWeights = new ConcurrentHashMap<>();

    private final Map<ActionTypeAvro, Double> actionTypeWeight = Map.of(
            ActionTypeAvro.VIEW, 0.4,
            ActionTypeAvro.REGISTER, 0.8,
            ActionTypeAvro.LIKE, 1.0
    );

    public List<EventSimilarityAvro> calculateSimilarity(UserActionAvro userAction) {
        Double newWeight = actionTypeWeight.get(userAction.getActionType());
        if (newWeight == null) {
            log.warn("Неизвестный тип действия: {}", userAction.getActionType());
            return Collections.emptyList();
        }

        Long eventId = userAction.getEventId();
        Long userId = userAction.getUserId();

        Map<Long, Double> eventWeights = userWeights.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>());
        Double prevWeight = eventWeights.getOrDefault(userId, 0.0);

        if (newWeight <= prevWeight) {
            return Collections.emptyList();
        }

        eventWeights.put(userId, newWeight);

        Double currentSum = eventSums.getOrDefault(eventId, 0.0);
        eventSums.put(eventId, currentSum - prevWeight + newWeight);

        List<EventSimilarityAvro> similarities = recalculateEventSimilarity(
                eventId, userId, prevWeight, newWeight, userAction.getTimestamp()
        );

        log.debug("Рассчитано сходство для события {}: {} пар", eventId, similarities.size());
        return similarities;
    }

    private List<EventSimilarityAvro> recalculateEventSimilarity(
            Long eventId, Long userId, Double oldWeight, Double newWeight, Instant timestamp) {

        List<EventSimilarityAvro> eventSimilarities = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : userWeights.entrySet()) {
            Long otherEventId = entry.getKey();

            if (otherEventId.equals(eventId)) {
                continue;
            }

            Double otherWeight = entry.getValue().get(userId);
            if (otherWeight == null) {
                continue;
            }

            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);
            double delta = newMin - oldMin;

            if (delta != 0) {
                updateMinWeight(eventId, otherEventId, delta);
            }

            double similarity = calculateSimilarity(eventId, otherEventId);

            eventSimilarities.add(buildSimilarity(eventId, otherEventId, similarity, timestamp));
        }

        return eventSimilarities;
    }

    private double calculateSimilarity(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        Map<Long, Double> innerMap = minWeights.get(first);
        Double sumMin = (innerMap != null) ? innerMap.get(second) : 0.0;

        if (sumMin == 0.0) {
            return 0.0;
        }

        Double sumA = eventSums.getOrDefault(eventA, 0.0);
        Double sumB = eventSums.getOrDefault(eventB, 0.0);

        if (sumA == 0.0 || sumB == 0.0) {
            return 0.0;
        }

        double similarity = sumMin / (Math.sqrt(sumA) * Math.sqrt(sumB));
        return Math.round(similarity * 100.0) / 100.0;
    }

    private EventSimilarityAvro buildSimilarity(long eventA, long eventB, double score, Instant timestamp) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(score)
                .setTimestamp(timestamp)
                .build();
    }

    private void updateMinWeight(Long eventA, Long eventB, Double delta) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeights
                .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .merge(second, delta, Double::sum);
    }


    @PreDestroy
    public void cleanup() {
        userWeights.clear();
        eventSums.clear();
        minWeights.clear();
    }

}