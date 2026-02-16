package ru.yandex.practicum.graduation.core.user.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.graduation.core.dto.UserDto;
import ru.yandex.practicum.graduation.core.user.dto.NewUserRequest;


import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Pageable pageable);

    UserDto addUser(NewUserRequest newUserRequest);

    void deleteUser(Long id);

    UserDto findUserById(Long userId);

    List<UserDto> findUsersById(List<Long> userIds);
}
