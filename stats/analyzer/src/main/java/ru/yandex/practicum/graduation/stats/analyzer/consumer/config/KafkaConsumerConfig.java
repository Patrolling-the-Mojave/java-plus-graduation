package ru.yandex.practicum.graduation.stats.analyzer.consumer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.graduation.stats.analyzer.consumer.deserializer.EventSimilarityDeserializer;
import ru.yandex.practicum.graduation.stats.analyzer.consumer.deserializer.UserActionDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, UserActionAvro> userActionConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> userActionListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, UserActionAvro>();
        factory.setConsumerFactory(userActionConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, EventSimilarityAvro> eventSimilarityConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> eventSimilarityListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro>();
        factory.setConsumerFactory(eventSimilarityConsumerFactory());
        return factory;
    }

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }
}
