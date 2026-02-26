package ru.yandex.practicum.graduation.stats.analyzer.service;


import ru.practicum.stats.dto.dto.RecommendationEvent;

import java.util.List;

public interface RecommendationService {
    List<RecommendationEvent> getRecommendationForUser(Long userId, long maxResults);

    List<RecommendationEvent> getSimilarEvents(Long eventId, Long userId, long maxResults);

    List<RecommendationEvent> getInteractionsCount(List<Long> eventIds);
}
