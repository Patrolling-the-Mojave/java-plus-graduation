package ru.yandex.practicum.graduation.core.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.client.CollectorClient;
import ru.yandex.practicum.graduation.core.event.dto.request.event.SearchOfEventByPublicDto;
import ru.yandex.practicum.graduation.core.event.dto.request.event.SortOfEvent;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventFullDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventShortDto;
import ru.yandex.practicum.graduation.core.event.service.EventPublicService;
import ru.yandex.practicum.stats.collector.event.ActionTypeProto;
import ru.yandex.practicum.stats.collector.event.UserActionProto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventPublicController {
    private final CollectorClient collectorClient;
    private final EventPublicService eventPublicService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(name = "text", required = false) String text,
                                         @RequestParam(name = "categories", required = false) List<Long> categories,
                                         @RequestParam(name = "paid", required = false) Boolean paid,
                                         @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(name = "onlyAvailable", defaultValue = "false")
                                         Boolean onlyAvailable,
                                         @RequestParam(name = "sort", required = false) SortOfEvent sort,
                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                         HttpServletRequest request) {
        SearchOfEventByPublicDto searchOfEventByPublicDto = SearchOfEventByPublicDto.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .build();
        Pageable pageable = PageRequest.of(from, size);
        return eventPublicService.getEvents(searchOfEventByPublicDto, pageable, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(
            @PathVariable @Positive Long id,
            @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId,
            HttpServletRequest request) {

        if (userId != null && userId > 0) {
            UserActionProto userActionProto = UserActionProto.newBuilder()
                    .setEventId(id)
                    .setUserId(userId)
                    .setActionType(ActionTypeProto.ACTION_VIEW)
                    .build();
            collectorClient.sendUserAction(userActionProto);
            log.debug("Отправлено действие просмотра: пользователь={}, мероприятие={}", userId, id);
        }

        EventFullDto dto = eventPublicService.getEvent(id, userId, request);
        return dto;
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendations(
            @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId,
            @Positive @RequestParam(defaultValue = "10") Integer maxResults) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("Требуется заголовок X-EWM-USER-ID с корректным ID пользователя");
        }
        return eventPublicService.getRecommendations(userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeEvent(
            @PathVariable @Positive Long eventId,
            @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("Требуется заголовок X-EWM-USER-ID с корректным ID пользователя");
        }
        UserActionProto userActionProto = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_LIKE)
                .build();
        collectorClient.sendUserAction(userActionProto);
    }
}
