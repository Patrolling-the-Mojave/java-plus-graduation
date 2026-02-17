package ru.yandex.practicum.graduation.core.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.graduation.core.dto.*;
import ru.yandex.practicum.graduation.core.dto.event.EventDto;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
import ru.yandex.practicum.graduation.core.dto.request.ConfirmedRequestsCountDto;
import ru.yandex.practicum.graduation.core.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.interaction.EventClient;
import ru.yandex.practicum.graduation.core.interaction.EventClientException;
import ru.yandex.practicum.graduation.core.interaction.UserClient;
import ru.yandex.practicum.graduation.core.interaction.UserClientException;
import ru.yandex.practicum.graduation.core.dto.exception.ConflictException;
import ru.yandex.practicum.graduation.core.request.exception.OwnershipMismatchException;
import ru.yandex.practicum.graduation.core.request.mapper.RequestMapper;
import ru.yandex.practicum.graduation.core.request.model.Request;
import ru.yandex.practicum.graduation.core.request.repository.RequestRepository;
import ru.yandex.practicum.graduation.core.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestMapper requestMapper;
    private final RequestRepository requestRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsByRequesterId(Long userId) {
        return requestMapper.toDto(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        try {
            UserDto userDto = userClient.findUserById(userId);

        } catch (Exception e) {
            throw new UserClientException("не удалось получить пользователя с id" + userId);
        }
        Request request = getRequestById(requestId);
        if (!request.getRequesterId().equals(userId)) {
            throw new OwnershipMismatchException("пользователь " + userId + " не отправлял запрос " + request.getId());
        }
        request.setStatus(Request.RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("параметр eventId обязателен");
        }
        EventDto event;
        try {
            event = eventClient.findEventById(eventId);
        } catch (Exception e) {
            throw new EventClientException("не удалось получить event с id " + eventId);
        }
        UserDto user;
        try {
            user = userClient.findUserById(userId);

        } catch (Exception e) {
            throw new UserClientException("не удалось получить пользователя с id" + userId);
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("запрос на участие в событии " + eventId + " уже создан");
        }
        if (event.getInitiatorId().equals(userId)) {
            throw new ConflictException(userId + " является инициатором события");
        }
        if (event.getState() != EventStateDto.PUBLISHED) {
            throw new ConflictException("нельзя участвовать в неопубликованном событии");
        }
//        if (requestRepository.countByEvent_Id(eventId) >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
//            throw new ConflictException("достигнут лимит запросов на участие");
//        }
        if (!event.getRequestModeration()) {
            int confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
            if (confirmedRequests >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
                throw new ConflictException("Достигнут лимит подтверждённых участников");
            }
        }
        Request request = Request
                .builder()
                .requesterId(userId)
                .eventId(eventId)
                .created(LocalDateTime.now())
                .status(event.getRequestModeration() ? Request.RequestStatus.PENDING : Request.RequestStatus.CONFIRMED)
                .build();
        if (event.getParticipantLimit() == 0) {
            request.setStatus(Request.RequestStatus.CONFIRMED);
        }
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(Long eventId) {
        return requestMapper.toDto(requestRepository.findAllByEventId(eventId));
    }

    @Override
    public List<ParticipationRequestDto> findAllByRequestIdInAndEventID(List<Long> requestIds, Long eventId) {
        return requestMapper.toDto(requestRepository.findAllByIdInAndEventId(requestIds, eventId));
    }

    @Override
    @Transactional
    public void confirmRequests(List<Long> requestsIds) {
        List<Request> requests = requestRepository.findAllByIdIn(requestsIds);
        requests.stream().forEach(request -> request.setStatus(Request.RequestStatus.CONFIRMED));
        requestRepository.saveAll(requests);
    }

    @Override
    public void rejectRequests(List<Long> requestIds) {
        List<Request> requests = requestRepository.findAllByIdIn(requestIds);
        requests.stream().forEach(request -> request.setStatus(Request.RequestStatus.REJECTED));
        requestRepository.saveAll(requests);
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("запрос с id " + requestId + " не найден"));
    }

    @Override
    public List<ConfirmedRequestsCountDto> countConfirmedRequests(List<Long> eventIds) {
        return requestRepository.countConfirmedRequestsByEventIds(eventIds);
    }

    @Override
    public Integer countConfirmedRequest(Long eventId) {
        return requestRepository.countByEvent_Id(eventId);
    }
}
