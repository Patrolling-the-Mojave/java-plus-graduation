package ru.yandex.practicum.graduation.stats.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.graduation.stats.collector.service.UserActionProducerService;
import ru.yandex.practicum.stats.collector.event.UserActionProto;
import ru.yandex.practicum.stats.collector.grpc.UserActionControllerGrpc;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionProducerService userActionProducerService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            userActionProducerService.produce(request);
        } catch (Exception e) {
            log.error("Ошибка обработки collectUserAction", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Ошибка сервера при обработке события")
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }
}
