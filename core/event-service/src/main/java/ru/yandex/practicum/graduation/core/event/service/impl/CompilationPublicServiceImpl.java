package ru.yandex.practicum.graduation.core.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.event.dto.response.compilation.CompilationDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventShortDto;
import ru.yandex.practicum.graduation.core.event.mapper.CompilationMapper;
import ru.yandex.practicum.graduation.core.event.mapper.EventMapper;
import ru.yandex.practicum.graduation.core.event.mapper.UserMapper;
import ru.yandex.practicum.graduation.core.event.model.Compilation;
import ru.yandex.practicum.graduation.core.event.model.Event;
import ru.yandex.practicum.graduation.core.event.repository.CompilationRepository;
import ru.yandex.practicum.graduation.core.event.service.CompilationPublicService;
import ru.yandex.practicum.graduation.core.interaction.UserClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationPublicServiceImpl implements CompilationPublicService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final UserClient userClient;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    @Override
    public List<CompilationDto> findAllByFilters(Boolean pinned, Pageable pageable) {
        log.info("запрос на поиск по фильтрам");
        Page<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable);
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }
        if (compilations.isEmpty()) {
            log.debug("по заданным фильтрам ничего не найдено");
            return Collections.emptyList();
        }
        List<Compilation> compilationList = compilations.getContent();
        Set<Event> allEvents = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .collect(Collectors.toSet());
        List<Long> initiatorIds = allEvents.stream().map(Event::getInitiatorId).toList();
        List<UserDto> userDtos = userClient.findUsersByIds(initiatorIds);
        Map<Long, UserDto> userByIdMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return compilations.stream()
                .map(compilation -> {
                    Set<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                            .map(event -> {
                                UserDto userDto = userByIdMap.get(event.getInitiatorId());
                                return eventMapper.toEventShortDto(event, userDto);
                            })
                            .collect(Collectors.toSet());

                    CompilationDto dto = compilationMapper.toDto(compilation);
                    dto.setEvents(eventShortDtos);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findById(Long compilationId) {
        log.debug("поиск подборки с id {}", compilationId);
        Compilation compilation = getById(compilationId);
        List<Long> initiatorIds = compilation.getEvents().stream().map(Event::getInitiatorId).toList();
        List<UserDto> userDtos = userClient.findUsersByIds(initiatorIds);
        Map<Long, UserDto> userByIdMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        Set<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                .map(event -> {
                    UserDto userDto = userByIdMap.get(event.getInitiatorId());
                    return eventMapper.toEventShortDto(event, userDto);
                })
                .collect(Collectors.toSet());
        CompilationDto dto = compilationMapper.toDto(compilation);
        dto.setEvents(eventShortDtos);

        return dto;
    }

    private Compilation getById(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("подборка с id " + compilationId + " не найдена"));
    }
}
