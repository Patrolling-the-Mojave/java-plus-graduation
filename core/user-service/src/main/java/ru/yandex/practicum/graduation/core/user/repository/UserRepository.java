package ru.yandex.practicum.graduation.core.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.graduation.core.user.model.User;

import java.util.List;


public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByIdIn(List<Long> userIds);
}
