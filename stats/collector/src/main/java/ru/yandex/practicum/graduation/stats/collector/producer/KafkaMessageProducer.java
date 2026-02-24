package ru.yandex.practicum.graduation.stats.collector.producer;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducer {
    private final KafkaProducer<Long, SpecificRecordBase> kafkaProducer;

    public <T extends SpecificRecordBase> void send(T event, String topic, Long key) {
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(topic, key, event);
        kafkaProducer.send(record);
    }
}
