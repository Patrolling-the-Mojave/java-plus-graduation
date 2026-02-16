package ru.yandex.practicum.graduation.core.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.graduation.core.dto.UserDto;
import ru.yandex.practicum.graduation.core.interaction.UserClient;
import ru.yandex.practicum.graduation.core.user.service.UserService;

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
}
