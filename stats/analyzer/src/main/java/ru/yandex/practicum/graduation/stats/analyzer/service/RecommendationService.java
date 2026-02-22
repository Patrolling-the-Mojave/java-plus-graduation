package ru.yandex.practicum.graduation.stats.analyzer.service;


import ru.practicum.stats.dto.dto.RecommendationEvent;

import java.util.List;

public interface RecommendationService {
    List<RecommendationEvent> getRecommendationForUser(Long userId, int maxResults);

    List<RecommendationEvent> getSimilarEvents(Long eventId, Long userId, int maxResults);

    List<RecommendationEvent> getInteractionsCount(List<Long> eventIds);
}
