package ru.yandex.practicum.graduation.core.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.graduation.core.dto.EventDto;
import ru.yandex.practicum.graduation.core.event.service.InternalEventService;
import ru.yandex.practicum.graduation.core.interaction.EventClient;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class InternalEventController implements EventClient {
    private final InternalEventService internalEventService;

    @Override
    @GetMapping("/{eventId}")
    public EventDto findEventById(@PathVariable Long eventId){
        return internalEventService.findEventById(eventId);
    }

}
