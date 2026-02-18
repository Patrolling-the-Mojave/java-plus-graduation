package ru.yandex.practicum.graduation.core.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.graduation.core.dto.exception.ConflictException;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.user.dto.NewUserRequest;
import ru.yandex.practicum.graduation.core.user.mapper.UserMapper;
import ru.yandex.practicum.graduation.core.user.model.User;
import ru.yandex.practicum.graduation.core.user.repository.UserRepository;
import ru.yandex.practicum.graduation.core.user.service.UserService;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImp implements UserService {
    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Pageable pageable) {
        if (ids == null || ids.isEmpty()) {
            List<UserDto> users = repository.findAll(pageable).stream()
                    .map(userMapper::toDto)
                    .toList();
            return users;
        }
        List<UserDto> users = repository.findAllById(ids).stream()
                .map(userMapper::toDto)
                .toList();
        return users;
    }

    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUserRequest) {
        log.info("Добавление нового пользователя " + newUserRequest);
        if (repository.existsByEmail(newUserRequest.getEmail())){
            throw new ConflictException("пользователь с email "+ newUserRequest.getEmail()+" уже существует");
        }
        User user = userMapper.toEntity(newUserRequest);
        User savedUser = repository.save(user);
        log.info("Пользователь добавлен: {}", savedUser);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Попытка удаления пользователя по ID: {}", userId);
        if (!repository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        repository.deleteById(userId);
        log.info("Пользователь с ID: {} удален", userId);
    }

    @Override
    public UserDto findUserById(Long userId) {
        User user = repository.findById(userId).orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> findUsersById(List<Long> userIds) {
        return repository.findAllById(userIds).stream().map(userMapper::toDto).toList();
    }
}
