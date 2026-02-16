package ru.yandex.practicum.graduation.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.graduation.core.event.dto.response.event.Location;
import ru.yandex.practicum.graduation.core.event.model.LocationEntity;


@Mapper(componentModel = "spring")
public interface LocationMapper {


    @Mapping(target = "id", ignore = true)
    LocationEntity toLocation(Location location);

    Location toLocationDto(LocationEntity locationEntity);
}
