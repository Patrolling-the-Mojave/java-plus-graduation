package ru.yandex.practicum.graduation.stats.analyzer.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.stats.dto.dto.RecommendationEvent;
import ru.yandex.practicum.graduation.stats.analyzer.service.RecommendationService;
import ru.yandex.practicum.stats.analyzer.grpc.RecommendationsControllerGrpc;
import ru.yandex.practicum.stats.analyzer.recommendations.InteractionsCountRequestProto;
import ru.yandex.practicum.stats.analyzer.recommendations.RecommendedEventProto;
import ru.yandex.practicum.stats.analyzer.recommendations.SimilarityEventsRequestProto;
import ru.yandex.practicum.stats.analyzer.recommendations.UserPredicationsRequestProto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getSimilarEvents(SimilarityEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendationEvent> similarEvents = recommendationService.getSimilarEvents(
                    request.getEventId(),
                    request.getUserId(),
                    request.getMaxResults()
            );

            for (RecommendationEvent event : similarEvents) {
                RecommendedEventProto proto = RecommendedEventProto.newBuilder()
                        .setEventId(event.getEventId())
                        .setScore(event.getScore())
                        .build();
                responseObserver.onNext(proto);
            }

            responseObserver.onCompleted();
            log.info("отправлено похожих мероприятий: {}", similarEvents.size());

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("ошибка поиска похожих мероприятий")
                    .asRuntimeException());
        }
    }

    @Override
    public void getRecommendationsForUser(UserPredicationsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendationEvent> recommendations = recommendationService.getRecommendationForUser(
                    request.getUserId(),
                    request.getMaxResults()
            );

            for (RecommendationEvent rec : recommendations) {
                RecommendedEventProto proto = RecommendedEventProto.newBuilder()
                        .setEventId(rec.getEventId())
                        .setScore(rec.getScore())
                        .build();
                responseObserver.onNext(proto);
            }
            responseObserver.onCompleted();
            log.info("отправлено рекомендаций: {}", recommendations.size());

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("ошибка генерации рекомендаций")
                    .asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<Long> eventIds = new ArrayList<>();
            for (int i = 0; i < request.getEventIdsCount(); i++) {
                eventIds.add(request.getEventIds(i));
            }

            List<RecommendationEvent> counts = recommendationService.getInteractionsCount(eventIds);

            for (RecommendationEvent count : counts) {
                RecommendedEventProto proto = RecommendedEventProto.newBuilder()
                        .setEventId(count.getEventId())
                        .setScore(count.getScore())
                        .build();
                responseObserver.onNext(proto);
            }

            responseObserver.onCompleted();
            log.info("отправлены суммы взаимодействий для {} ивентов", counts.size());

        } catch (Exception e) {
            log.error("ошибка получения суммы взаимодействий", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("ошибка получения суммы взаимодействий")
                    .asRuntimeException());
        }
    }
}
