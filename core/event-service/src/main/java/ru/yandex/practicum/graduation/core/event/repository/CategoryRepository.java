package ru.yandex.practicum.graduation.core.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.graduation.core.event.model.Category;


public interface CategoryRepository extends JpaRepository<Category, Long> {
}
