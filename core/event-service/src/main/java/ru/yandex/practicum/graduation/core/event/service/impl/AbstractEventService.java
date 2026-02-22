package ru.yandex.practicum.graduation.core.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import ru.practicum.stats.client.AnalyzerClient;
import ru.practicum.stats.client.StatClient;
import ru.practicum.stats.dto.dto.RecommendationEvent;
import ru.practicum.stats.dto.dto.ViewStatsDto;
import ru.yandex.practicum.graduation.core.dto.request.ConfirmedRequestsCountDto;
import ru.yandex.practicum.graduation.core.event.model.Event;
import ru.yandex.practicum.graduation.core.interaction.RequestClient;
import ru.yandex.practicum.graduation.core.interaction.RequestClientException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventService {
    protected final RequestClient requestClient;
    protected final AnalyzerClient analyzerClient;


    protected Map<Long, Double> getEventsRating(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        try {
            List<RecommendationEvent> ratings = analyzerClient.getInteractionsCount(eventIds);
            return ratings.stream()
                    .collect(Collectors.toMap(
                            RecommendationEvent::getEventId,
                            RecommendationEvent::getScore,
                            (a, b) -> a
                    ));
        } catch (Exception e) {
            log.warn("Ошибка при получении рейтинга мероприятий: {}", e.getMessage());
            return events.stream()
                    .collect(Collectors.toMap(Event::getId, event -> 0.0));
        }
    }

    protected Double getEventRating(Long eventId) {
        try {
            List<RecommendationEvent> ratings = analyzerClient.getInteractionsCount(List.of(eventId));
            return ratings.stream()
                    .findFirst()
                    .map(RecommendationEvent::getScore)
                    .orElse(0.0);
        } catch (Exception e) {
            log.warn("Ошибка при получении рейтинга для мероприятия {}: {}", eventId, e.getMessage());
            return 0.0;
        }
    }

    protected Long extractEventIdFromUri(String uri) {
        try {
            return Long.parseLong(uri.replace("/events/", ""));
        } catch (NumberFormatException e) {
            log.warn("Не удалось извлечь ID события из URI: {}", uri);
            return 0L;
        }
    }

    protected Map<Long, Integer> getConfirmedRequests(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        List<ConfirmedRequestsCountDto> results;
        try {
            results = requestClient.countConfirmedRequest(eventIds);
        } catch (Exception e) {
            throw new RequestClientException("не удалось посчитать запросы", e);
        }
        Map<Long, Long> confirmedRequestsMap = results.stream()
                .collect(Collectors.toMap(
                        ConfirmedRequestsCountDto::getEventId,
                        ConfirmedRequestsCountDto::getCount
                ));
        return eventIds.stream()
                .collect(Collectors.toMap(
                        eventId -> eventId,
                        eventId -> confirmedRequestsMap.getOrDefault(eventId, 0L).intValue()
                ));
    }

    protected Integer getConfirmedRequests(Long eventId) {
        Integer count;
        try {
            count = requestClient.countConfirmedRequest(eventId);
        } catch (Exception e) {
            throw new RequestClientException("не удалось посчитать запросы", e);
        }
        return count;
    }
}
