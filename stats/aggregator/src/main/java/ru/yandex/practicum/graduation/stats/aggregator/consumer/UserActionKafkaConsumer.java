package ru.yandex.practicum.graduation.stats.aggregator.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.graduation.stats.aggregator.exception.AggregatorException;
import ru.yandex.practicum.graduation.stats.aggregator.service.EventSimilarityService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActionKafkaConsumer {
    private final EventSimilarityService similarityService;
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    @Value("${spring.kafka.topics.stats.event-similarity.v1}")
    private String similarityTopic;

    @KafkaListener(topics = "${spring.kafka.topics.stats.user-action.v1}")
    public void processAction(ConsumerRecord<String, UserActionAvro> record) {
        UserActionAvro userAction = record.value();
        List<EventSimilarityAvro> similarities = similarityService.calculateSimilarity(userAction);
        for (EventSimilarityAvro similarity : similarities) {
            try {
                String key = similarity.getEventA() + "" + similarity.getEventB();
                kafkaTemplate.send(similarityTopic, null, similarity.getTimestamp().toEpochMilli(), key, similarity)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("не удалось отправить сходство {} в топик",
                                        similarity, ex);
                            } else {
                                log.debug("сходство {} успешно отправлено", similarity);
                            }
                        });
            } catch (Exception e) {
                throw new AggregatorException("не удалось обработать сходство " + similarity, e);
            }
        }

    }
}
