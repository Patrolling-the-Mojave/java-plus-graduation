package ru.yandex.practicum.graduation.stats.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.graduation.stats.collector.producer.KafkaMessageProducer;
import ru.yandex.practicum.graduation.stats.collector.producer.mapper.UserActionMapper;
import ru.yandex.practicum.stats.collector.event.UserActionProto;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionProducerService {
    private final KafkaMessageProducer kafkaMessageProducer;

    @Value("${spring.kafka.topics.stats.user-action.v1}")
    private String topic;

    public void produce(UserActionProto userActionProto) {
        UserActionAvro userActionAvro = UserActionMapper.toAvro(userActionProto);
        String key = String.valueOf(userActionProto.getUserId());
        kafkaMessageProducer.send(userActionAvro, topic, key);
    }
}
