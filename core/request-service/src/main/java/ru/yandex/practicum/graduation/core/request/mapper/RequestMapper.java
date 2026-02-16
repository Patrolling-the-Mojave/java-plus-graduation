package ru.yandex.practicum.graduation.core.request.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.graduation.core.dto.ParticipationRequestDto;
import ru.yandex.practicum.graduation.core.request.model.Request;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    ParticipationRequestDto toDto(Request request);

    List<ParticipationRequestDto> toDto(List<Request> requests);
}
