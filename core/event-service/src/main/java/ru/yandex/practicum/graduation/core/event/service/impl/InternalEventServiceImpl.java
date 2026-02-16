package ru.yandex.practicum.graduation.core.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.graduation.core.dto.EventDto;
import ru.yandex.practicum.graduation.core.dto.NotFoundException;
import ru.yandex.practicum.graduation.core.event.mapper.EventMapper;
import ru.yandex.practicum.graduation.core.event.model.Event;
import ru.yandex.practicum.graduation.core.event.repository.EventRepository;
import ru.yandex.practicum.graduation.core.event.service.InternalEventService;

@Service
@Slf4j
@RequiredArgsConstructor
public class InternalEventServiceImpl implements InternalEventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public EventDto findEventById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("ивент с id " + eventId + " не найден"));
        return eventMapper.toEventDto(event);
    }
}
