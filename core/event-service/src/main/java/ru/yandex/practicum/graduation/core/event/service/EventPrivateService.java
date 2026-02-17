package ru.yandex.practicum.graduation.core.event.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.graduation.core.event.dto.request.event.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.graduation.core.event.dto.request.event.NewEventDto;
import ru.yandex.practicum.graduation.core.event.dto.request.event.UpdateEventUserRequest;
import ru.yandex.practicum.graduation.core.event.dto.request.event.UserIdAndEventIdDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventFullDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventRequestStatusUpdateResult;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventShortDto;
import ru.yandex.practicum.graduation.core.dto.request.ParticipationRequestDto;


import java.util.List;

public interface EventPrivateService {

    List<EventShortDto> getEvents(Long userId, Pageable pageable);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEvent(Long eventId, Long userId);

    EventFullDto updateEvent(UserIdAndEventIdDto userIdAndEventIdDto, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequests(UserIdAndEventIdDto userIdAndEventIdDto, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}
