package ru.yandex.practicum.graduation.core.event.service;

import ru.yandex.practicum.graduation.core.dto.EventDto;

public interface InternalEventService {
    EventDto findEventById(Long eventId);
}
