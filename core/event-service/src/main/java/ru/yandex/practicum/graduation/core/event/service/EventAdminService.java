package ru.yandex.practicum.graduation.core.event.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.graduation.core.event.dto.request.event.SearchOfEventByAdminDto;
import ru.yandex.practicum.graduation.core.event.dto.request.event.UpdateEventAdminRequest;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventFullDto;


import java.util.List;

public interface EventAdminService {
    List<EventFullDto> getEvents(SearchOfEventByAdminDto searchOfEventByAdminDto,
                                 Pageable pageable);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);
}
