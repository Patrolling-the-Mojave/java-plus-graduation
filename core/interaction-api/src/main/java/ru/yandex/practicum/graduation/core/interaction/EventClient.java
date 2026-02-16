package ru.yandex.practicum.graduation.core.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.graduation.core.dto.EventDto;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/internal/events/{eventId}")
    EventDto findEventById(@PathVariable Long eventId);
}
