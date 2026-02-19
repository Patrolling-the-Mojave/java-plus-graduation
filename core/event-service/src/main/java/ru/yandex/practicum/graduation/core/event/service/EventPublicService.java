package ru.yandex.practicum.graduation.core.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.graduation.core.event.dto.request.event.SearchOfEventByPublicDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventFullDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventShortDto;


import java.util.List;

public interface EventPublicService {

    List<EventShortDto> getEvents(SearchOfEventByPublicDto searchOfEventByPublicDto, Pageable pageable, HttpServletRequest request);

    EventFullDto getEvent(Long id, HttpServletRequest request);
}
