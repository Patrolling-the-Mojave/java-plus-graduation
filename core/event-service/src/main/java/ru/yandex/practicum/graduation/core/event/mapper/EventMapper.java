package ru.yandex.practicum.graduation.core.event.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import ru.yandex.practicum.graduation.core.dto.EventStateDto;
import ru.yandex.practicum.graduation.core.dto.event.EventDto;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.event.dto.request.event.NewEventDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventFullDto;
import ru.yandex.practicum.graduation.core.event.dto.response.event.EventShortDto;
import ru.yandex.practicum.graduation.core.event.model.Category;
import ru.yandex.practicum.graduation.core.event.model.Event;
import ru.yandex.practicum.graduation.core.event.model.LocationEntity;


@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, LocationMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "views", ignore = true)
    EventShortDto toEventShortDto(Event event, UserDto initiator);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "locationEntity", source = "savedLocationEntity")
    @Mapping(target = "initiatorId", source = "initiatorId")
    @Mapping(target = "paid", defaultValue = "false")
    @Mapping(target = "id",ignore = true )
    @Mapping(target = "participantLimit", defaultValue = "0")
    @Mapping(target = "requestModeration", defaultValue = "true")
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "description", source = "newEventDto.description")
    @Mapping(target = "annotation", source = "newEventDto.annotation")
    @Mapping(target = "eventDate", source = "newEventDto.eventDate")
    @Mapping(target = "title", source = "newEventDto.title")
    Event toEventFromNewEventDto(NewEventDto newEventDto, Long initiatorId, Category category, LocationEntity savedLocationEntity);

    @Mapping(target = "initiator", source = "user")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "location", source = "event.locationEntity")
    EventFullDto toEventFullDto(Event event, UserDto user);

    @Mapping(target = "state", qualifiedByName = "mapEventState")
    EventDto toEventDto(Event event);

    @Named("mapEventState")
    default EventStateDto mapEventState(Event.EventState eventState){
        if (eventState == null){
            return null;
        }
        return EventStateDto.valueOf(eventState.name());
    }
}
