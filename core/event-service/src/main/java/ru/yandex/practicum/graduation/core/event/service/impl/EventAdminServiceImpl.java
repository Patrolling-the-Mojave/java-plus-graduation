package ru.yandex.practicum.graduation.core.event.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.client.AnalyzerClient;
import ru.yandex.practicum.graduation.core.dto.exception.ConflictException;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.event.dto.request.event.SearchOfEventByAdminDto;
import ru.yandex.practicum.graduation.core.event.dto.request.event.StateAction;
import ru.yandex.practicum.graduation.core.event.dto.request.event.UpdateEventAdminRequest;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventFullDto;
import ru.yandex.practicum.graduation.core.event.mapper.EventMapper;
import ru.yandex.practicum.graduation.core.event.mapper.LocationMapper;
import ru.yandex.practicum.graduation.core.event.model.Category;
import ru.yandex.practicum.graduation.core.event.model.Event;
import ru.yandex.practicum.graduation.core.event.model.LocationEntity;
import ru.yandex.practicum.graduation.core.event.model.QEvent;
import ru.yandex.practicum.graduation.core.event.repository.CategoryRepository;
import ru.yandex.practicum.graduation.core.event.repository.EventRepository;
import ru.yandex.practicum.graduation.core.event.repository.LocationRepository;
import ru.yandex.practicum.graduation.core.event.service.EventAdminService;
import ru.yandex.practicum.graduation.core.interaction.RequestClient;
import ru.yandex.practicum.graduation.core.interaction.UserClient;
import ru.yandex.practicum.graduation.core.interaction.UserClientException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EventAdminServiceImpl extends AbstractEventService implements EventAdminService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final UserClient userClient;


    public EventAdminServiceImpl(RequestClient requestClient,
                                 AnalyzerClient analyzerClient,
                                 EventMapper eventMapper,
                                 UserClient userClient,
                                 LocationMapper locationMapper,
                                 EventRepository eventRepository,
                                 CategoryRepository categoryRepository,
                                 LocationRepository locationRepository) {
        super(requestClient, analyzerClient);
        this.eventMapper = eventMapper;
        this.userClient = userClient;
        this.locationMapper = locationMapper;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public List<EventFullDto> getEvents(SearchOfEventByAdminDto searchDto, Pageable pageable) {
        log.debug("Админ поиск событий по критериям: {}", searchDto);
        Predicate predicate = buildPredicate(searchDto);
        Page<Event> eventsPage = eventRepository.findAll(predicate, pageable);
        if (eventsPage.isEmpty()) {
            log.debug("События по заданным критериям не найдены");
            return Collections.emptyList();
        }
        List<Event> events = eventsPage.getContent();
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).toList();
        List<UserDto> userDtos;
        try {
            userDtos = userClient.findUsersByIds(initiatorIds);
        } catch (Exception e) {
            throw new UserClientException("не удалось получить dto инициаторов", e);
        }
        Map<Long, UserDto> initiatorByIdMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        Map<Long, Double> ratings = getEventsRating(events);
        Map<Long, Integer> confirmedRequests = getConfirmedRequests(events);
        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toEventFullDto(event, initiatorByIdMap.get(event.getInitiatorId()));
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));
                    dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.debug("Админ обновление события {}: {}", eventId, updateRequest);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id=%d не найдено", eventId)));
        updateEventFields(event, updateRequest);
        if (updateRequest.getStateAction() != null) {
            processAdminStateAction(event, updateRequest.getStateAction());
        }
        if (updateRequest.getEventDate() != null) {
            validateEventDateForAdmin(event, updateRequest.getEventDate());
            event.setEventDate(updateRequest.getEventDate());
        }
        Event updatedEvent = eventRepository.save(event);

        Double rating = getEventRating(eventId);
        Integer confirmedRequests = requestClient.countConfirmedRequest(eventId);
        updatedEvent.setConfirmedRequests(confirmedRequests);
        UserDto userDto;
        try {
            userDto = userClient.findUserById(event.getInitiatorId());
        } catch (Exception e) {
            throw new UserClientException("не удалось получить пользователя", e);
        }
        EventFullDto result = eventMapper.toEventFullDto(updatedEvent, userDto);
        result.setRating(rating);
        result.setConfirmedRequests(confirmedRequests);

        log.info("Событие {} успешно обновлено администратором", eventId);
        return result;
    }

    private Predicate buildPredicate(SearchOfEventByAdminDto searchDto) {
        QEvent event = QEvent.event;
        BooleanBuilder predicate = new BooleanBuilder();

        if (searchDto.getUsers() != null && !searchDto.getUsers().isEmpty()) {
            predicate.and(event.initiatorId.in(searchDto.getUsers()));
        }

        if (searchDto.getStates() != null && !searchDto.getStates().isEmpty()) {
            List<Event.EventState> states = searchDto.getStates().stream()
                    .map(Event.EventState::valueOf)
                    .collect(Collectors.toList());
            predicate.and(event.state.in(states));
        }

        if (searchDto.getCategories() != null && !searchDto.getCategories().isEmpty()) {
            predicate.and(event.category.id.in(searchDto.getCategories()));
        }

        if (searchDto.getRangeStart() != null) {
            predicate.and(event.eventDate.goe(searchDto.getRangeStart()));
        }
        if (searchDto.getRangeEnd() != null) {
            predicate.and(event.eventDate.loe(searchDto.getRangeEnd()));
        }

        return predicate;
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest updateRequest) {
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

    private void processAdminStateAction(Event event, StateAction stateAction) {
        switch (stateAction) {
            case PUBLISH_EVENT:
                validateEventCanBePublished(event);
                event.setState(Event.EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                validateEventCanBeRejected(event);
                event.setState(Event.EventState.CANCELED);
                break;
            default:
                throw new ValidationException("Неверное действие: " + stateAction);
        }
    }

    private void validateEventCanBePublished(Event event) {
        if (event.getState() != Event.EventState.PENDING) {
            throw new ConflictException("Не можем опубликовать событие, так как оно не в том состоянии: " + event.getState());
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Не можем опубликовать событие, так как оно начинается слишком рано");
        }
    }

    private void validateEventCanBeRejected(Event event) {
        if (event.getState() == Event.EventState.PUBLISHED) {
            throw new ConflictException("Нельзя отклонить, так как уже опубликовано");
        }
    }

    private void validateEventDateForAdmin(Event event, LocalDateTime newEventDate) {
        if (event.getState() == Event.EventState.PUBLISHED &&
                newEventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
        }
    }
}
