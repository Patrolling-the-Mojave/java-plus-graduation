package ru.yandex.practicum.graduation.stats.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.graduation.stats.analyzer.exception.AnalyzerServiceException;
import ru.yandex.practicum.graduation.stats.analyzer.mapper.UserActionMapper;
import ru.yandex.practicum.graduation.stats.analyzer.model.UserInteraction;
import ru.yandex.practicum.graduation.stats.analyzer.repository.UserInteractionRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionKafkaConsumer {
    private final UserInteractionRepository userInteractionRepository;

    @KafkaListener(topics = "${spring.kafka.topics.stats.user-action.v1}",
            groupId = "analyzer-user-action-group",
            containerFactory = "userActionListenerContainerFactory")
    public void pollUserInteraction(ConsumerRecord<String, UserActionAvro> record) {
        try {
            UserInteraction userInteraction = UserActionMapper.toEntity(record.value());
            userInteractionRepository.save(userInteraction);
        } catch (Exception e) {
            throw new AnalyzerServiceException("произошла ошибка при обработке действий пользователя", e);
        }
    }
}
