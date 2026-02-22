package ru.yandex.practicum.graduation.core.event.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.stats.client.AnalyzerClient;
import ru.practicum.stats.client.CollectorClient;
import ru.practicum.stats.client.StatClient;
import ru.practicum.stats.dto.dto.EndpointHitDto;
import ru.practicum.stats.dto.dto.RecommendationEvent;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.dto.exception.ValidationException;
import ru.yandex.practicum.graduation.core.event.dto.request.event.SearchOfEventByPublicDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventFullDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventShortDto;
import ru.yandex.practicum.graduation.core.event.mapper.EventMapper;
import ru.yandex.practicum.graduation.core.event.model.Event;
import ru.yandex.practicum.graduation.core.event.model.QEvent;
import ru.yandex.practicum.graduation.core.event.repository.EventRepository;
import ru.yandex.practicum.graduation.core.event.service.EventPublicService;
import ru.yandex.practicum.graduation.core.interaction.RequestClient;
import ru.yandex.practicum.graduation.core.interaction.RequestClientException;
import ru.yandex.practicum.graduation.core.interaction.UserClient;
import ru.yandex.practicum.graduation.core.interaction.UserClientException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EventPublicServiceImpl extends AbstractEventService implements EventPublicService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient userClient;
    private final CollectorClient collectorClient;

    public EventPublicServiceImpl(RequestClient requestClient,
                                  AnalyzerClient analyzerClient,
                                  CollectorClient collectorClient,
                                  EventRepository eventRepository,
                                  EventMapper eventMapper, UserClient userClient) {
        super(requestClient, analyzerClient);
        this.eventMapper = eventMapper;
        this.collectorClient = collectorClient;
        this.eventRepository = eventRepository;
        this.userClient = userClient;
    }

    @Override
    public List<EventShortDto> getEvents(SearchOfEventByPublicDto searchDto, Pageable pageable, HttpServletRequest request) {
        log.debug("Публичный поиск событий по критериям: {}", searchDto);
        if (searchDto.getRangeStart() != null
                && searchDto.getRangeEnd() != null
                && searchDto.getRangeEnd().isBefore(searchDto.getRangeStart())) {
            throw new ValidationException("Дата окончания события должна быть после даты начала");
        }
        Predicate predicate = buildPredicate(searchDto);
        Page<Event> eventsPage = eventRepository.findAll(predicate, pageable);
        if (eventsPage.isEmpty()) {
            log.debug("События по заданным критериям не найдены");
            return Collections.emptyList();
        }
        List<Event> events = eventsPage.getContent();
        List<UserDto> userDtos;
        try {
            userDtos = userClient.findUsersByIds(events.stream().map(Event::getInitiatorId).toList());
        } catch (Exception e) {
            throw new UserClientException("не удалось получить данные от сервиса пользователей");
        }
        Map<Long, UserDto> userByIdMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        Map<Long, Double> ratings = getEventsRating(events);
        Map<Long, Integer> confirmedRequests = getConfirmedRequests(events);
        List<EventShortDto> result = events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event, userByIdMap.get(event.getInitiatorId()));
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));
                    dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0));
                    return dto;
                })
                .toList();
        return result;
    }

    @Override
    public EventFullDto getEvent(Long id, Long userId, HttpServletRequest request) {
        log.debug("Получение публичного события {}", id);
        Event event = eventRepository.findByIdAndState(id, Event.EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id=%d не было найдено или не опубликовано", id)));
        Integer confirmedRequests;
        try {
            confirmedRequests = requestClient.countConfirmedRequest(id);
        } catch (Exception e) {
            throw new RequestClientException("не удалось получить запросы на участие в ивенте");
        }
        UserDto userDto;
        try {
            userDto = userClient.findUserById(event.getInitiatorId());
        } catch (Exception e) {
            throw new UserClientException("не удалось получить данные от сервиса пользователей");
        }
        if (userDto == null) {
            throw new NotFoundException("пользователь с id " + event.getInitiatorId() + " не найден");
        }
        event.setConfirmedRequests(confirmedRequests);
        Double rating = getEventRating(id);
        EventFullDto result = eventMapper.toEventFullDto(event, userDto);
        result.setRating(rating);
        result.setConfirmedRequests(confirmedRequests);
        log.debug("Событие {} найдено", id);
        return result;
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, int maxResults) {
        List<RecommendationEvent> recommendations = analyzerClient.getRecommendationsForUser(userId, maxResults);
        if (recommendations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> eventIds = recommendations.stream()
                .map(RecommendationEvent::getEventId)
                .collect(Collectors.toList());

        List<Event> events = eventRepository.findAllById(eventIds);
        Map<Long, Event> eventMap = events.stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .collect(Collectors.toList());

        List<UserDto> userDtos = userClient.findUsersByIds(initiatorIds);
        Map<Long, UserDto> userByIdMap = userDtos.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return recommendations.stream()
                .map(rec -> {
                    Event event = eventMap.get(rec.getEventId());
                    if (event == null) {
                        return null;
                    }
                    EventShortDto dto = eventMapper.toEventShortDto(event, userByIdMap.get(event.getInitiatorId()));
                    dto.setRating(rec.getScore());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Predicate buildPredicate(SearchOfEventByPublicDto searchDto) {
        QEvent event = QEvent.event;
        BooleanBuilder predicate = new BooleanBuilder();

        predicate.and(event.state.eq(Event.EventState.PUBLISHED));

        if (StringUtils.hasText(searchDto.getText())) {
            String text = searchDto.getText().toLowerCase();
            predicate.and(event.annotation.toLowerCase().contains(text)
                    .or(event.description.toLowerCase().contains(text)));
        }

        if (searchDto.getCategories() != null && !searchDto.getCategories().isEmpty()) {
            predicate.and(event.category.id.in(searchDto.getCategories()));
        }

        if (searchDto.getPaid() != null) {
            predicate.and(event.paid.eq(searchDto.getPaid()));
        }

        if (searchDto.getRangeStart() != null) {
            predicate.and(event.eventDate.goe(searchDto.getRangeStart()));
        }
        if (searchDto.getRangeEnd() != null) {
            predicate.and(event.eventDate.loe(searchDto.getRangeEnd()));
        }

        if (searchDto.getRangeStart() == null && searchDto.getRangeEnd() == null) {
            predicate.and(event.eventDate.after(LocalDateTime.now()));
        }

        if (Boolean.TRUE.equals(searchDto.getOnlyAvailable())) {
            predicate.and(event.participantLimit.eq(0)
                    .or(event.participantLimit.gt(event.confirmedRequests)));
        }

        return predicate;
    }

}
