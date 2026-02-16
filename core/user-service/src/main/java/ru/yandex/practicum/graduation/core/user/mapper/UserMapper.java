package ru.yandex.practicum.graduation.core.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.graduation.core.dto.UserDto;
import ru.yandex.practicum.graduation.core.user.dto.NewUserRequest;
import ru.yandex.practicum.graduation.core.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest newUserRequest);

    UserDto toDto(User user);
}
