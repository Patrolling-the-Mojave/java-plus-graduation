package ru.yandex.practicum.graduation.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.event.dto.request.compilation.NewCompilationDto;
import ru.yandex.practicum.graduation.core.event.dto.response.compilation.CompilationDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventShortDto;
import ru.yandex.practicum.graduation.core.event.model.Compilation;
import ru.yandex.practicum.graduation.core.event.model.Event;

import java.util.List;
import java.util.Set;


@Mapper(componentModel = "spring", uses = {EventMapper.class, UserMapper.class})
public interface CompilationMapper {


    @Mapping(target = "events", source = "events")
    @Mapping(target = "id", ignore = true)
    Compilation toEntity(NewCompilationDto newCompilation, Set<Event> events);

    @Mapping(target = "events", ignore = true)
    CompilationDto toDto(Compilation compilation);

    List<CompilationDto> toDto(List<Compilation> compilations);

}
