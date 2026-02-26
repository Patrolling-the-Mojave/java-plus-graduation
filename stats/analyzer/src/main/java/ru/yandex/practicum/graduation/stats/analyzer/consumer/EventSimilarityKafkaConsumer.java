package ru.yandex.practicum.graduation.stats.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.graduation.stats.analyzer.exception.AnalyzerServiceException;
import ru.yandex.practicum.graduation.stats.analyzer.mapper.EventSimilarityMapper;
import ru.yandex.practicum.graduation.stats.analyzer.model.EventSimilarity;
import ru.yandex.practicum.graduation.stats.analyzer.repository.EventSimilarityRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityKafkaConsumer {
    private final EventSimilarityRepository eventSimilarityRepository;

    @KafkaListener(topics = "${spring.kafka.topics.stats.events-similarity.v1}",
            groupId = "analyzer-event-similarity-group",
            containerFactory = "eventSimilarityListenerContainerFactory")
    public void pollEventSimilarity(ConsumerRecord<Long, EventSimilarityAvro> record) {
        try {
            EventSimilarityAvro avro = record.value();
            long eventA = Math.min(avro.getEventA(), avro.getEventB());
            long eventB = Math.max(avro.getEventA(), avro.getEventB());

            Optional<EventSimilarity> existing = eventSimilarityRepository
                    .findByEventAAndEventB(eventA, eventB);

            if (existing.isPresent()) {
                EventSimilarity similarity = existing.get();
                similarity.setSimilarityScore(avro.getScore());
                similarity.setUpdatedAt(avro.getTimestamp().toEpochMilli());
                eventSimilarityRepository.save(similarity);
            } else {
                EventSimilarity similarity = EventSimilarity.builder()
                        .eventA(eventA)
                        .eventB(eventB)
                        .similarityScore(avro.getScore())
                        .updatedAt(avro.getTimestamp().toEpochMilli())
                        .build();
                eventSimilarityRepository.save(similarity);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать сходство ивентов", e);
        }
    }
}
