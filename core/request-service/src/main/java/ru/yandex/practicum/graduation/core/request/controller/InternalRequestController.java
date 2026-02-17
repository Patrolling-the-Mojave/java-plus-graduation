package ru.yandex.practicum.graduation.core.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.graduation.core.dto.request.ConfirmedRequestsCountDto;
import ru.yandex.practicum.graduation.core.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.graduation.core.interaction.RequestClient;
import ru.yandex.practicum.graduation.core.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/internal/requests")
public class InternalRequestController implements RequestClient {
    private final RequestService requestService;

    @GetMapping("/countConfirmed")
    @Override
    public List<ConfirmedRequestsCountDto> countConfirmedRequest(@RequestBody List<Long> eventIds) {
        return requestService.countConfirmedRequests(eventIds);
    }

    @Override
    @GetMapping("/countConfirmed/{eventId}")
    public Integer countConfirmedRequest(@PathVariable Long eventId) {
        return requestService.countConfirmedRequest(eventId);
    }

    @Override
    @GetMapping("/{eventId}")
    public List<ParticipationRequestDto> findRequestsByEventId(@PathVariable Long eventId) {
        return requestService.getRequestsByEventId(eventId);
    }

    @Override
    @PostMapping("/{eventId}")
    public List<ParticipationRequestDto> findAllByIdInAndEventId(@RequestBody List<Long> requestIds, @PathVariable Long eventId) {
        return requestService.findAllByRequestIdInAndEventID(requestIds, eventId);
    }

    @PostMapping("/confirm")
    @Override
    public void confirmRequests(@RequestBody List<Long> requestIds) {
        requestService.confirmRequests(requestIds);
    }

    @PostMapping("/reject")
    @Override
    public void rejectRequests(@RequestBody List<Long> requestIds) {
        requestService.rejectRequests(requestIds);
    }
}
