package ru.yandex.practicum.graduation.core.event.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.client.StatClient;
import ru.yandex.practicum.graduation.core.dto.exception.ConflictException;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
import ru.yandex.practicum.graduation.core.dto.exception.ValidationException;
import ru.yandex.practicum.graduation.core.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.event.dto.request.event.*;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventFullDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventRequestStatusUpdateResult;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventShortDto;
import ru.yandex.practicum.graduation.core.event.dto.response.request.RequestStatusDto;
import ru.yandex.practicum.graduation.core.event.mapper.EventMapper;
import ru.yandex.practicum.graduation.core.event.mapper.LocationMapper;
import ru.yandex.practicum.graduation.core.event.model.Category;
import ru.yandex.practicum.graduation.core.event.model.Event;
import ru.yandex.practicum.graduation.core.event.model.LocationEntity;
import ru.yandex.practicum.graduation.core.event.repository.CategoryRepository;
import ru.yandex.practicum.graduation.core.event.repository.EventRepository;
import ru.yandex.practicum.graduation.core.event.repository.LocationRepository;
import ru.yandex.practicum.graduation.core.event.service.EventPrivateService;
import ru.yandex.practicum.graduation.core.interaction.RequestClient;
import ru.yandex.practicum.graduation.core.interaction.RequestClientException;
import ru.yandex.practicum.graduation.core.interaction.UserClient;
import ru.yandex.practicum.graduation.core.interaction.UserClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EventPrivateServiceImpl extends AbstractEventService implements EventPrivateService {

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    public EventPrivateServiceImpl(RequestClient requestClient,
                                   StatClient statClient,
                                   EventRepository eventRepository,
                                   UserClient userClient,
                                   LocationMapper locationMapper,
                                   EventMapper eventMapper,
                                   CategoryRepository categoryRepository,
                                   LocationRepository locationRepository) {
        super(requestClient, statClient);
        this.eventRepository = eventRepository;
        this.userClient = userClient;
        this.locationMapper = locationMapper;
        this.eventMapper = eventMapper;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Pageable pageable) {
        validateUserExisted(userId);
        log.debug("Получаем события пользователя {} с пагинацией: {}", userId, pageable);
        Page<Event> eventsPage = eventRepository.findByInitiatorIdOrderByCreatedOnDesc(userId, pageable);
        if (eventsPage.isEmpty()) {
            log.debug("События для пользователя {} не найдены", userId);
            return Collections.emptyList();
        }
        List<Event> events = eventsPage.getContent();
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).toList();
        List<UserDto> userDtos;
        try {
            userDtos = userClient.findUsersByIds(initiatorIds);
        } catch (Exception e) {
            throw new UserClientException("не удалось получить инициаторов", e);
        }
        Map<Long, UserDto> userByIdMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        Map<Long, Long> views = getEventsViews(events);
        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event, userByIdMap.get(event.getInitiatorId()));
                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.debug("Создание события пользователем {}: {}", userId, newEventDto);

        UserDto user;
        try {
            user = userClient.findUserById(userId);
        } catch (Exception e) {
            throw new UserClientException("Пользователь c userId " + userId + " не найден");
        }
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException(
                        "Категория c id " + newEventDto.getCategory() + " не найдена"));
        validateEventDate(newEventDto.getEventDate());
        LocationEntity locationEntity = locationMapper.toLocation(newEventDto.getLocation());
        LocationEntity savedLocationEntity = locationRepository.save(locationEntity);
        Event event = eventMapper.toEventFromNewEventDto(newEventDto, userId, category, savedLocationEntity);
        event.setConfirmedRequests(0);
        Event savedEvent = eventRepository.save(event);
        log.info("Событие создано успешно: ID {}", savedEvent.getId());
        EventFullDto result = eventMapper.toEventFullDto(savedEvent, user);
        result.setViews(0L);
        return result;
    }

    @Override
    public EventFullDto getEvent(Long eventId, Long userId) {
        UserDto user = getUserFromUserService(userId);
        Event event = validateEventOfInitiator(eventId, userId);
        Integer confirmedRequests;
        try {
            confirmedRequests = requestClient.countConfirmedRequest(eventId);
        } catch (Exception e) {
            throw new RequestClientException("произошла ошибка при обращении к сервису запросов", e);
        }
        event.setConfirmedRequests(confirmedRequests);
        Long views = getEventViews(eventId);
        EventFullDto result = eventMapper.toEventFullDto(event, user);
        result.setViews(views);
        log.debug("Событие {} пользователя {} найдено", eventId, userId);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(UserIdAndEventIdDto userIdAndEventIdDto, UpdateEventUserRequest updateEventUserRequest) {
        Long userId = userIdAndEventIdDto.getUserId();
        Long eventId = userIdAndEventIdDto.getEventId();
        log.debug("Обновление события {} пользователя {}: {}", eventId, userId, updateEventUserRequest);
        UserDto userDto = getUserFromUserService(userId);
        Event event = validateEventOfInitiator(eventId, userId);
        validateEventCanBeUpdated(event);
        updateEventFields(event, updateEventUserRequest);
        if (updateEventUserRequest.getEventDate() != null) {
            validateEventDate(updateEventUserRequest.getEventDate());
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            processStateAction(event, updateEventUserRequest.getStateAction());
        }
        Integer confirmedRequests = requestClient.countConfirmedRequest(eventId);
        event.setConfirmedRequests(confirmedRequests);
        Event updatedEvent = eventRepository.save(event);
        Long views = getEventViews(eventId);
        EventFullDto result = eventMapper.toEventFullDto(updatedEvent, userDto);
        result.setViews(views);
        log.info("Событие {} пользователя {} успешно обновлено", eventId, userId);
        return result;
    }

    private void updateEventFields(Event event, UpdateEventUserRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Категория с id=%d не найдена", updateRequest.getCategory())));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getLocation() != null) {
            LocationEntity locationEntity = locationMapper.toLocation(updateRequest.getLocation());
            LocationEntity savedLocation = locationRepository.save(locationEntity);
            event.setLocationEntity(savedLocation);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    private void processStateAction(Event event, StateAction stateAction) {
        switch (stateAction) {
            case SEND_TO_REVIEW:
                event.setState(Event.EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                event.setState(Event.EventState.CANCELED);
                break;
            default:
                throw new ValidationException("Неверное действие: " + stateAction);
        }
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        log.debug("Получение запросов на участие в событии {} пользователя {}", eventId, userId);
        validateUserExisted(userId);
        Event event = validateEventOfInitiator(eventId, userId);
        List<ParticipationRequestDto> requests;
        try {
            requests = requestClient.findRequestsByEventId(eventId);
        } catch (Exception e) {
            throw new RequestClientException("не удалось получить запросы на участие в ивенте " + eventId);
        }
        if (requests.isEmpty()) {
            log.debug("Запросы на участие в событии {} не найдены", eventId);
            return Collections.emptyList();
        }
        return requests;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequests(UserIdAndEventIdDto userIdAndEventIdDto, EventRequestStatusUpdateRequest updateRequest) {
        Long userId = userIdAndEventIdDto.getUserId();
        Long eventId = userIdAndEventIdDto.getEventId();
        log.debug("Обработка изменения статуса заявок для события {} пользователя {}: {}",
                eventId, userId, updateRequest);
        validateUserExisted(userId);
        Event event = validateEventOfInitiator(eventId, userId);
        if (!isModerationRequired(event)) {
            throw new ConflictException("Подтверждение заявок не требуется для этого события");
        }
        List<ParticipationRequestDto> requestsToProcess = getRequestsToProcess(updateRequest.getRequestIds().stream().toList(), eventId);
        validateRequestsCanBeProcessed(requestsToProcess, event, updateRequest.getStatus());
        EventRequestStatusUpdateResult result = processRequests(requestsToProcess, event, updateRequest.getStatus());
        log.info("Статусы заявок для события {} обновлены: подтверждено {}, отклонено {}",
                eventId, result.getConfirmedRequests().size(), result.getRejectedRequests().size());
        return result;
    }

    private void validateEventDate(LocalDateTime eventDate) {
        LocalDateTime minAllowedDate = LocalDateTime.now().plusHours(2);
        if (eventDate.isBefore(minAllowedDate)) {
            throw new ValidationException(
                    String.format("Дата события должна быть не раньше чем через 2 часа. " +
                                    "Текущее время: %s, указанное время: %s",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            eventDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            );
        }
    }

    private void validateUserExisted(Long userId) {
        log.debug("Проверяем, что пользователь с userId {} существует", userId);
        UserDto user;
        try {
            user = userClient.findUserById(userId);
        } catch (Exception e) {
            throw new UserClientException("произошла ошибка при обращении с сервису пользователе");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь c userId " + userId + " не найден");
        }
    }

    private Event validateEventOfInitiator(Long eventId, Long userId) {
        log.debug("Проверяем, что событие {} создано пользователем с userId {}", eventId, userId);
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id=%d не найдено для пользователя с id=%d", eventId, userId)));
    }

    private void validateEventCanBeUpdated(Event event) {
        if (event.getState() != Event.EventState.PENDING && event.getState() != Event.EventState.CANCELED) {
            throw new ConflictException("Изменить можно только отмененные события или события в состоянии ожидания модерации");
        }
    }

    private boolean isModerationRequired(Event event) {
        return event.getParticipantLimit() != 0 && event.getRequestModeration();
    }

    private List<ParticipationRequestDto> getRequestsToProcess(List<Long> requestIds, Long eventId) {
        List<ParticipationRequestDto> requests;
        try {
            requests = requestClient.findAllByIdInAndEventId(requestIds, eventId);
        } catch (Exception e) {
            throw new RequestClientException("не удалось получить запросы от сервиса", e);
        }
        if (requests.size() != requestIds.size()) {
            throw new NotFoundException("Некоторые запросы не найдены или не принадлежат событию");
        }
        return requests;
    }

    private void validateRequestsCanBeProcessed(List<ParticipationRequestDto> requests, Event event, RequestStatusDto newStatus) {
        requests.forEach(request -> {
            if (!request.getStatus().equals(RequestStatusDto.PENDING.name())) {
                throw new ConflictException(
                        String.format("Запрос %d уже обработан (статус: %s)",
                                request.getId(), request.getStatus()));
            }
        });

        if (newStatus == RequestStatusDto.CONFIRMED) {
            int confirmedCount;
            try {
                confirmedCount = requestClient.countConfirmedRequest(event.getId());
            } catch (Exception e) {
                throw new RequestClientException("не удалось получить данные от сервиса запросов", e);
            }
            int availableSlots = event.getParticipantLimit() - confirmedCount;

            if (availableSlots <= 0) {
                throw new ConflictException("Лимит участников для события исчерпан");
            }

            if (requests.size() > availableSlots) {
                throw new ConflictException(
                        String.format("Недостаточно свободных мест: доступно %d, запрошено %d",
                                availableSlots, requests.size()));
            }
        }
    }

    private EventRequestStatusUpdateResult processRequests(List<ParticipationRequestDto> requests, Event event,
                                                           RequestStatusDto newStatus) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        if (newStatus == RequestStatusDto.CONFIRMED) {
            processConfirmation(requests, event, result);
        } else if (newStatus == RequestStatusDto.REJECTED) {
            processRejection(requests, result);
        }

        return result;
    }

    private void processConfirmation(List<ParticipationRequestDto> requests, Event event,
                                     EventRequestStatusUpdateResult result) {
        int confirmedCount;
        try{
            confirmedCount = requestClient.countConfirmedRequest(event.getId());
        } catch (Exception e){
            throw new RequestClientException("не удалось получить данные от сервиса запросов", e);
        }
        int availableSlots = event.getParticipantLimit() - confirmedCount;

        List<ParticipationRequestDto> toConfirm = requests.stream()
                .limit(availableSlots)
                .peek(request -> request.setStatus(RequestStatusDto.CONFIRMED.name()))
                .toList();

        List<ParticipationRequestDto> toReject = requests.stream()
                .skip(availableSlots)
                .peek(request -> request.setStatus(RequestStatusDto.REJECTED.name()))
                .toList();

        try{
            requestClient.confirmRequests(toConfirm.stream().map(ParticipationRequestDto::getId).toList());
            requestClient.rejectRequests(toReject.stream().map(ParticipationRequestDto::getId).toList());
        }catch (Exception e){
            throw new RequestClientException("не удалось изменить статус запросов", e);
        }

        result.setConfirmedRequests(toConfirm);
        result.setRejectedRequests(toReject);
    }

    private void processRejection(List<ParticipationRequestDto> requests, EventRequestStatusUpdateResult result) {
        requests.forEach(request -> {
            request.setStatus(RequestStatusDto.REJECTED.name());
        });

        try{
            requestClient.rejectRequests(requests.stream().map(ParticipationRequestDto::getId).toList());
        } catch (Exception e){
            throw new RequestClientException("не удалось отклонить запросы");
        }

        result.setRejectedRequests(requests);
        result.setConfirmedRequests(Collections.emptyList());
    }

    private UserDto getUserFromUserService(Long userId) {
        log.debug("Проверяем, что пользователь с userId {} существует", userId);
        UserDto user;
        try {
            user = userClient.findUserById(userId);
        } catch (Exception e) {
            throw new UserClientException("произошла ошибка при обращении с сервису пользователе");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь c userId " + userId + " не найден");
        }
        return user;
    }
}
