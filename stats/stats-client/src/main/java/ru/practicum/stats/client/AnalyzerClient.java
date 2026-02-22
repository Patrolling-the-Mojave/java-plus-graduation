package ru.practicum.stats.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.dto.RecommendationEvent;
import ru.yandex.practicum.stats.analyzer.grpc.RecommendationsControllerGrpc;
import ru.yandex.practicum.stats.analyzer.recommendations.InteractionsCountRequestProto;
import ru.yandex.practicum.stats.analyzer.recommendations.RecommendedEventProto;
import ru.yandex.practicum.stats.analyzer.recommendations.SimilarityEventsRequestProto;
import ru.yandex.practicum.stats.analyzer.recommendations.UserPredicationsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class AnalyzerClient {

    @GrpcClient("analyzer")
    RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationClient;

    public List<RecommendationEvent> getRecommendationsForUser(long userId, int maxResults) {
        try {
            log.debug("Запрос рекомендаций для пользователя {}: лимит={}", userId, maxResults);

            UserPredicationsRequestProto request = UserPredicationsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = recommendationClient.getRecommendationsForUser(request);
            List<RecommendationEvent> recommendations = toStream(iterator)
                    .map(proto -> new RecommendationEvent(proto.getEventId(), proto.getScore()))
                    .collect(Collectors.toList());

            log.info("Получено рекомендаций для пользователя {}: {}", userId, recommendations.size());
            return recommendations;

        } catch (StatusRuntimeException e) {
            log.error("Ошибка gRPC вызова Analyzer: {}", e.getStatus(), e);
            throw new StatsClientException("Не удалось получить рекомендации", e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при запросе рекомендаций", e);
            throw new StatsClientException("Неожиданная ошибка при работе с сервисом рекомендаций", e);
        }
    }

    public List<RecommendationEvent> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            log.debug("Запрос похожих мероприятий: мероприятие={}, пользователь={}, лимит={}",
                    eventId, userId, maxResults);

            SimilarityEventsRequestProto request = SimilarityEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = recommendationClient.getSimilarEvents(request);
            List<RecommendationEvent> similarEvents = toStream(iterator)
                    .map(proto -> new RecommendationEvent(proto.getEventId(), proto.getScore()))
                    .collect(Collectors.toList());

            log.info("Получено похожих мероприятий для {}: {}", eventId, similarEvents.size());
            return similarEvents;

        } catch (StatusRuntimeException e) {
            log.error("Ошибка gRPC вызова Analyzer: {}", e.getStatus(), e);
            throw new StatsClientException("Не удалось найти похожие мероприятия", e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при запросе похожих мероприятий", e);
            throw new StatsClientException("Неожиданная ошибка при работе с сервисом рекомендаций", e);
        }
    }

    public List<RecommendationEvent> getInteractionsCount(List<Long> eventIds) {
        try {
            log.debug("Запрос суммы взаимодействий для {} мероприятий", eventIds.size());

            InteractionsCountRequestProto.Builder requestBuilder = InteractionsCountRequestProto.newBuilder();
            eventIds.forEach(requestBuilder::addEventIds);
            InteractionsCountRequestProto request = requestBuilder.build();

            Iterator<RecommendedEventProto> iterator = recommendationClient.getInteractionsCount(request);
            List<RecommendationEvent> counts = toStream(iterator)
                    .map(proto -> new RecommendationEvent(proto.getEventId(), proto.getScore()))
                    .collect(Collectors.toList());

            log.info("Получены суммы взаимодействий для {} мероприятий", counts.size());
            return counts;

        } catch (StatusRuntimeException e) {
            log.error("Ошибка gRPC вызова Analyzer: {}", e.getStatus(), e);
            throw new StatsClientException("Не удалось получить сумму взаимодействий", e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при запросе суммы взаимодействий", e);
            throw new StatsClientException("Неожиданная ошибка при работе с сервисом рекомендаций", e);
        }
    }

    private Stream<RecommendedEventProto> toStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
