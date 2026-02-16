package ru.yandex.practicum.graduation.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.graduation.core.dto.UserDto;
import ru.yandex.practicum.graduation.core.event.dto.request.compilation.NewCompilationDto;
import ru.yandex.practicum.graduation.core.event.dto.response.compilation.CompilationDto;
import ru.yandex.practicum.graduation.core.event.model.Compilation;
import ru.yandex.practicum.graduation.core.event.model.Event;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    @Mapping(target = "id", ignore = true)
    Compilation toEntity(NewCompilationDto newCompilation, Set<Event> events);

    default CompilationDto toDto(Compilation compilation, Map<Long, UserDto> initiatorById) {
        return CompilationDto
                .builder()
                .events(compilation.getEvents().stream()
                        .map(event -> EventMapper.INSTANCE.toEventShortDto(event, initiatorById.get(event.getInitiatorId())))
                        .collect(Collectors.toSet()))
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    default List<CompilationDto> toDto(List<Compilation> compilations, Map<Long, UserDto> initiatorByIdMap) {
        return compilations.stream().map(compilation -> toDto(compilation, initiatorByIdMap)).toList();
    }
}
