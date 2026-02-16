package ru.yandex.practicum.graduation.core.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.graduation.core.dto.NotFoundException;
import ru.yandex.practicum.graduation.core.dto.UserDto;
import ru.yandex.practicum.graduation.core.event.dto.response.compilation.CompilationDto;
import ru.yandex.practicum.graduation.core.event.mapper.CompilationMapper;
import ru.yandex.practicum.graduation.core.event.model.Compilation;
import ru.yandex.practicum.graduation.core.event.model.Event;
import ru.yandex.practicum.graduation.core.event.repository.CompilationRepository;
import ru.yandex.practicum.graduation.core.event.service.CompilationPublicService;
import ru.yandex.practicum.graduation.core.interaction.UserClient;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationPublicServiceImpl implements CompilationPublicService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final UserClient userClient;

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
        Set<Event> events = new HashSet<>();
        compilationList.stream().peek(compilation -> events.addAll(compilation.getEvents())).toList();
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).toList();
        List<UserDto> userDtos = userClient.findUsersByIds(initiatorIds);
        Map<Long, UserDto> userDtoByIdMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return compilationMapper.toDto(compilationList, userDtoByIdMap);
    }

    @Override
    public CompilationDto findById(Long compilationId) {
        log.debug("поиск подборки с id {}", compilationId);
        Compilation compilation = getById(compilationId);
        List<Long> initiatorIds = compilation.getEvents().stream().map(Event::getInitiatorId).toList();
        List<UserDto> userDtos = userClient.findUsersByIds(initiatorIds);
        Map<Long, UserDto> userDtoByIdMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return compilationMapper.toDto(compilation, userDtoByIdMap);
    }

    private Compilation getById(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("подборка с id " + compilationId + " не найдена"));
    }
}
