package ru.yandex.practicum.graduation.stats.collector.producer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.collector.event.ActionTypeProto;
import ru.yandex.practicum.stats.collector.event.UserActionProto;

import java.time.Instant;

@Component
public class UserActionMapper {

    public static UserActionAvro toAvro(UserActionProto proto) {
        Instant ts = proto.hasTimestamp()
                ? Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos())
                : Instant.now();

        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(toAvroActionType(proto.getActionType()))
                .setTimestamp(ts)
                .build();
    }

    private static ActionTypeAvro toAvroActionType(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            default -> throw new IllegalArgumentException("Неизвестный тип действия: " + proto);
        };
    }


}
