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

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityKafkaConsumer {
    private final EventSimilarityRepository eventSimilarityRepository;

    @KafkaListener(topics = "${spring.kafka.topics.stats.events-similarity.v1}",
            groupId = "analyzer-event-similarity-group",
            containerFactory = "eventSimilarityListenerContainerFactory")
    public void pollEventSimilarity(ConsumerRecord<String, EventSimilarityAvro> record) {
        try {
            EventSimilarity entity = EventSimilarityMapper.toEntity(record.value());
            eventSimilarityRepository.save(entity);
        } catch (Exception e) {
            throw new AnalyzerServiceException("не удалось обработать сходство ивентов", e);
        }
    }
}
