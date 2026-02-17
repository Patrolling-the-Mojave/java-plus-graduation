package ru.yandex.practicum.graduation.core.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.interaction.UserClient;
import ru.yandex.practicum.graduation.core.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class UserInternalController implements UserClient {
    private final UserService userService;

    @GetMapping
    @Override
    public UserDto findUserById(@PathVariable("/{userId}") Long userId) {
        return userService.findUserById(userId);
    }

    @Override
    @PostMapping
    public List<UserDto> findUsersByIds(@RequestBody List<Long> userIds) {
        return userService.findUsersById(userIds);
    }
}
