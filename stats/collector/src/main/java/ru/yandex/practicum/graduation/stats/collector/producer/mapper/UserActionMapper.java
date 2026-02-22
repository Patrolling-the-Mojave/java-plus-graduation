package ru.yandex.practicum.graduation.stats.collector.producer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.collector.event.ActionTypeProto;
import ru.yandex.practicum.stats.collector.event.UserActionProto;

import java.time.Instant;

@Component
public class UserActionMapper {

    public static UserActionAvro toAvro(UserActionProto userActionProto) {
        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setTimestamp(Instant.ofEpochSecond(userActionProto.getTimestamp().getSeconds(),
                        userActionProto.getTimestamp().getNanos()))
                .setActionType(toAvroActionType(userActionProto.getActionType()))
                .build();
    }

    private static ActionTypeAvro toAvroActionType(ActionTypeProto actionTypeProto) {
        return switch (actionTypeProto) {
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case UNRECOGNIZED -> null;
        };
    }


}
