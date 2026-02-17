package ru.yandex.practicum.graduation.core.request.service;


import ru.yandex.practicum.graduation.core.dto.request.ConfirmedRequestsCountDto;
import ru.yandex.practicum.graduation.core.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto addRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequestsByRequesterId(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    List<ConfirmedRequestsCountDto> countConfirmedRequests(List<Long> eventIds);

    Integer countConfirmedRequest(Long eventId);

    List<ParticipationRequestDto> getRequestsByEventId(Long eventId);

    List<ParticipationRequestDto> findAllByRequestIdInAndEventID(List<Long> requestIds, Long eventId);

    void confirmRequests(List<Long> requestsIds);

    void rejectRequests(List<Long> requestIds);


}
