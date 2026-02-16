package ru.yandex.practicum.graduation.core.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.graduation.core.dto.UserDto;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/internal/users/{userId}")
    UserDto findUserById(@PathVariable Long userId);

    @GetMapping("/internal/users")
    List<UserDto> findUsersByIds(@RequestBody List<Long> userIds);
}
