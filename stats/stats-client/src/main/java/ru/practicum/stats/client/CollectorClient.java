package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.stats.collector.event.UserActionProto;
import ru.yandex.practicum.stats.collector.grpc.UserActionControllerGrpc;

@Slf4j
@Service
public class CollectorClient {

    @GrpcClient("collector")
    UserActionControllerGrpc.UserActionControllerBlockingStub collectorGrpc;

    public void sendUserAction(UserActionProto userActionProto) {
        try {
            collectorGrpc.collectUserAction(userActionProto);
            log.info("Действие отправлено в Collector: пользователь={}, мероприятие={}",
                    userActionProto.getUserId(), userActionProto.getEventId());
        } catch (Exception e) {
            throw new StatsClientException("ошибка при работе с сервисом статистики", e);
        }
    }
}
