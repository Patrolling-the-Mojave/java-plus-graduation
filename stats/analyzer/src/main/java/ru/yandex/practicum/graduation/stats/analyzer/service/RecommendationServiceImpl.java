package ru.yandex.practicum.graduation.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.dto.RecommendationEvent;
import ru.yandex.practicum.graduation.stats.analyzer.model.EventSimilarity;
import ru.yandex.practicum.graduation.stats.analyzer.model.UserInteraction;
import ru.yandex.practicum.graduation.stats.analyzer.repository.EventSimilarityRepository;
import ru.yandex.practicum.graduation.stats.analyzer.repository.UserInteractionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final EventSimilarityRepository similarityRepository;
    private final UserInteractionRepository interactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationEvent> getSimilarEvents(Long eventId, Long userId, int maxResults) {
        log.debug("Поиск похожих мероприятий для {} (пользователь: {})", eventId, userId);

        List<EventSimilarity> similarities = new ArrayList<>();
        similarities.addAll(similarityRepository.findByEventA(eventId));
        similarities.addAll(similarityRepository.findByEventB(eventId));

        Set<Long> viewedEvents = interactionRepository.findAllByUserId(userId).stream()
                .map(UserInteraction::getEventId)
                .collect(Collectors.toSet());

        return similarities.stream()
                .map(sim -> extractOtherEvent(sim, eventId))
                .filter(event -> !viewedEvents.contains(event.getEventId()))
                .sorted(Comparator.comparingDouble(RecommendationEvent::getScore).reversed())
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private RecommendationEvent extractOtherEvent(EventSimilarity similarity, Long sourceEventId) {
        Long targetEventId = similarity.getEventA().equals(sourceEventId)
                ? similarity.getEventB()
                : similarity.getEventA();

        return new RecommendationEvent(targetEventId, similarity.getSimilarityScore());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationEvent> getRecommendationForUser(Long userId, int maxResults) {
        List<UserInteraction> recentInteractions = interactionRepository
                .findTopByUserId(userId, org.springframework.data.domain.PageRequest.of(0, 10));

        if (recentInteractions.isEmpty()) {
            log.debug("Пользователь {} ещё не взаимодействовал ни с одним мероприятием", userId);
            return Collections.emptyList();
        }

        Set<Long> viewedEvents = recentInteractions.stream()
                .map(UserInteraction::getEventId)
                .collect(Collectors.toSet());

        Map<Long, Double> candidateScores = new HashMap<>();

        for (UserInteraction interaction : recentInteractions) {
            Long sourceEventId = interaction.getEventId();
            Double userWeight = interaction.getMaxWeight();

            List<EventSimilarity> similarities = new ArrayList<>();
            similarities.addAll(similarityRepository.findByEventA(sourceEventId));
            similarities.addAll(similarityRepository.findByEventB(sourceEventId));

            for (EventSimilarity similarity : similarities) {
                Long targetEventId = extractOtherEvent(similarity, sourceEventId).getEventId();
                if (viewedEvents.contains(targetEventId)) {
                    continue;
                }
                double weightedScore = userWeight * similarity.getSimilarityScore();
                candidateScores.merge(targetEventId, weightedScore, Double::sum);
            }
        }

        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> new RecommendationEvent(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationEvent> getInteractionsCount(List<Long> eventIds) {
        log.debug("Получение суммы весов для {} мероприятий", eventIds.size());

        if (eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserInteraction> interactions = interactionRepository.findAllByEventIdIn(eventIds);
        Map<Long, Double> sums = interactions.stream()
                .collect(Collectors.groupingBy(
                        UserInteraction::getEventId,
                        Collectors.summingDouble(UserInteraction::getMaxWeight)
                ));
        return sums.entrySet().stream()
                .map(entry -> new RecommendationEvent(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void upsertSimilarity(Long eventA, Long eventB, Double similarityScore, Long updatedAt) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        Optional<EventSimilarity> existing = similarityRepository
                .findByEventAAndEventB(first, second);

        if (existing.isPresent()) {
            EventSimilarity similarity = existing.get();
            similarity.setSimilarityScore(similarityScore);
            similarity.setUpdatedAt(updatedAt);
            similarityRepository.save(similarity);
            log.trace("Обновлено сходство: ({}, {}) = {}", first, second, similarityScore);
        } else {
            EventSimilarity similarity = EventSimilarity.builder()
                    .eventA(first)
                    .eventB(second)
                    .similarityScore(similarityScore)
                    .updatedAt(updatedAt)
                    .build();
            similarityRepository.save(similarity);
            log.trace("Создано сходство: ({}, {}) = {}", first, second, similarityScore);
        }
    }
}
