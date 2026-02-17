package ru.yandex.practicum.graduation.core.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.event.dto.request.compilation.NewCompilationDto;
import ru.yandex.practicum.graduation.core.event.dto.request.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.graduation.core.event.dto.response.compilation.CompilationDto;
import ru.yandex.practicum.graduation.core.event.mapper.CompilationMapper;
import ru.yandex.practicum.graduation.core.event.model.Compilation;
import ru.yandex.practicum.graduation.core.event.model.Event;
import ru.yandex.practicum.graduation.core.event.repository.CompilationRepository;
import ru.yandex.practicum.graduation.core.event.repository.EventRepository;
import ru.yandex.practicum.graduation.core.event.service.CompilationAdminService;
import ru.yandex.practicum.graduation.core.event.util.Updater;
import ru.yandex.practicum.graduation.core.interaction.UserClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationAdminServiceImpl implements CompilationAdminService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final UserClient userClient;

    @Override
    @Transactional
    public CompilationDto add(NewCompilationDto newCompilation) {
        log.debug("добавление новой подборки{}", newCompilation);
        Set<Event> events = eventRepository.findAllByIdIn(newCompilation.getEvents());
        Compilation compilation = compilationMapper.toEntity(newCompilation, events);
        compilationRepository.save(compilation);
        List<Long>  initiatorIds = events.stream().map(Event::getId).toList();
        List<UserDto> userDtos = userClient.findUsersByIds(initiatorIds);
        Map<Long, UserDto> userByIdMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return compilationMapper.toDto(compilation, userByIdMap);
    }

    @Override
    @Transactional
    public void deleteById(Long compilationId) {
        log.debug("удаление подборки с id{}", compilationId);
        compilationRepository.deleteById(compilationId);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compilationId, UpdateCompilationRequest updatedCompilation) {
        log.debug("обновление подборки с id{}", compilationId);
        Compilation oldCompilation = getById(compilationId);
        Updater.update(updatedCompilation.getEvents(), () -> oldCompilation.setEvents(eventRepository.findAllByIdIn(updatedCompilation.getEvents())));
        Updater.update(updatedCompilation.getTitle(), () -> oldCompilation.setTitle(updatedCompilation.getTitle()));
        Updater.update(updatedCompilation.getPinned(), () -> oldCompilation.setPinned(updatedCompilation.getPinned()));
        log.info("обновленная подборка{}", updatedCompilation);
        List<Long> initiatorIds = oldCompilation.getEvents().stream().map(Event::getInitiatorId).toList();
        List<UserDto> userDtos = userClient.findUsersByIds(initiatorIds);
        Map<Long, UserDto> userByIdMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return compilationMapper.toDto(oldCompilation, userByIdMap);
    }

    private Compilation getById(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("подборка с id " + compilationId + " не найдена"));
    }
}
