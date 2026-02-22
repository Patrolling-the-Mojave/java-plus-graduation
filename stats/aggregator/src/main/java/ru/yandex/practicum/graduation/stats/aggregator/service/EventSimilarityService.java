package ru.yandex.practicum.graduation.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.graduation.stats.aggregator.mapper.EventSimilarityMapper;

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

    private final Map<ActionTypeAvro, Double> actionTypeWeight = Map.of(ActionTypeAvro.VIEW, 0.4, ActionTypeAvro.REGISTER, 0.8, ActionTypeAvro.LIKE, 1.0);

    public List<EventSimilarityAvro> calculateSimilarity(UserActionAvro userAction) {
        Double newWeight = actionTypeWeight.get(userAction.getActionType());
        Double prevWeight = userWeights
                .getOrDefault(userAction.getEventId(), new ConcurrentHashMap<>())
                .get(userAction.getUserId());
        if (prevWeight == null) {
            prevWeight = 0.0;
        }
        if (newWeight <= prevWeight) {
            return Collections.emptyList();
        }
        userWeights.get(userAction.getEventId()).put(userAction.getUserId(), newWeight);
        if (eventSums.containsKey(userAction.getEventId())) {
            Double prevSum = eventSums.get(userAction.getEventId());
            eventSums.put(userAction.getEventId(), prevSum + newWeight - prevWeight);
        }
        List<EventSimilarityAvro> similarities = recalculateEventSimilarity(userAction.getEventId(), userAction.getUserId(), prevWeight, newWeight, userAction.getTimestamp());
        log.debug("произведен расчет косинусного сходства для ивентов {}", similarities);
        return similarities;
    }

    private List<EventSimilarityAvro> recalculateEventSimilarity(Long eventId, Long userId, Double oldWeight, Double newWeight, Instant timestamp) {
        List<EventSimilarityAvro> eventSimilarities = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userWeights.entrySet()) {
            if (entry.getKey().equals(eventId)) {
                continue;
            }
            Double eventWeight = entry.getValue().get(userId);
            if (eventWeight == null) {
                continue;
            }

            double oldMin = Math.min(oldWeight, eventWeight);
            double newMin = Math.min(newWeight, eventWeight);
            double delta = newMin - oldMin;

            if (newMin != oldMin) {
                updateMinWeight(eventId, entry.getKey(), delta);
            }
            double similarity = calculateSimilarity(eventId, entry.getKey());
            if (similarity > 0) {
                EventSimilarityAvro similarityAvro = EventSimilarityMapper.toSimilarity(eventId, entry.getKey(), similarity, timestamp);
                eventSimilarities.add(similarityAvro);
            }
        }
        return eventSimilarities;
    }

    private double calculateSimilarity(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        Double sumMin = minWeights
                .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .get(second);

        if (sumMin == null || sumMin == 0.0) {
            return 0.0;
        }

        Double sumA = eventSums.get(eventA);
        Double sumB = eventSums.get(eventB);

        if (sumA == null || sumB == null || sumA == 0.0 || sumB == 0.0) {
            return 0.0;
        }

        double similarity = sumMin / (Math.sqrt(sumA) * Math.sqrt(sumB));
        return Math.round(similarity * 1000.0) / 1000.0;
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
}
