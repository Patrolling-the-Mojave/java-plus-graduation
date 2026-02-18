package ru.yandex.practicum.graduation.core.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.graduation.core.dto.request.ConfirmedRequestsCountDto;
import ru.yandex.practicum.graduation.core.dto.request.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service")
public interface RequestClient {

    @PostMapping("/internal/requests/countConfirmed")
    List<ConfirmedRequestsCountDto> countConfirmedRequest(@RequestBody List<Long> eventIds);

    @GetMapping("/internal/requests/countConfirmed/{eventId}")
    Integer countConfirmedRequest(@PathVariable Long eventId);

    @GetMapping("/internal/requests/{eventId}")
    List<ParticipationRequestDto> findRequestsByEventId(@PathVariable Long eventId);

    @PostMapping("/internal/requests/{eventId}")
    List<ParticipationRequestDto> findAllByIdInAndEventId(@RequestBody List<Long> requestIds, @PathVariable Long eventId);

    @PostMapping("/internal/requests/confirm")
    void confirmRequests(@RequestBody List<Long> requestIds);

    @PostMapping("/internal/requests/reject")
    void rejectRequests(@RequestBody List<Long> requestIds);
}
