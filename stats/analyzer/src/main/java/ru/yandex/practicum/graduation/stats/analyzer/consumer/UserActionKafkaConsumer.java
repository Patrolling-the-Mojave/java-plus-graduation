package ru.yandex.practicum.graduation.stats.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.graduation.stats.analyzer.exception.AnalyzerServiceException;
import ru.yandex.practicum.graduation.stats.analyzer.mapper.UserActionMapper;
import ru.yandex.practicum.graduation.stats.analyzer.model.UserInteraction;
import ru.yandex.practicum.graduation.stats.analyzer.repository.UserInteractionRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionKafkaConsumer {
    private final UserInteractionRepository userInteractionRepository;

    @KafkaListener(topics = "${spring.kafka.topics.stats.user-action.v1}",
            groupId = "analyzer-user-action-group",
            containerFactory = "userActionListenerContainerFactory")
    public void pollUserInteraction(ConsumerRecord<Long, UserActionAvro> record) {
        try {
            UserActionAvro avro = record.value();
            Long userId = avro.getUserId();
            Long eventId = avro.getEventId();
            Double newWeight = toWeight(avro.getActionType());
            Long updatedAt = avro.getTimestamp().toEpochMilli();

            Optional<UserInteraction> existing = userInteractionRepository
                    .findByUserIdAndEventId(userId, eventId);

            if (existing.isPresent()) {
                UserInteraction interaction = existing.get();
                if (newWeight > interaction.getMaxWeight()) {
                    interaction.setMaxWeight(newWeight);
                    interaction.setUpdatedAt(updatedAt);
                    userInteractionRepository.save(interaction);
                    log.debug("Обновлено взаимодействие: user={}, event={}, weight={}",
                            userId, eventId, newWeight);
                }
            } else {
                UserInteraction interaction = UserInteraction.builder()
                        .userId(userId)
                        .eventId(eventId)
                        .maxWeight(newWeight)
                        .updatedAt(updatedAt)
                        .build();
                userInteractionRepository.save(interaction);
                log.debug("Создано взаимодействие: user={}, event={}, weight={}",
                        userId, eventId, newWeight);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки действия пользователя", e);
        }
    }

    private Double toWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> 0.0;
        };
    }
}
