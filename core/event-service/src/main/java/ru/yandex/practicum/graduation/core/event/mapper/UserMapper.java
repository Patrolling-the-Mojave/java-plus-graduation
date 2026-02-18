package ru.yandex.practicum.graduation.core.event.mapper;


import org.mapstruct.Mapper;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.dto.user.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserShortDto toShortDto(UserDto userDto);
}
