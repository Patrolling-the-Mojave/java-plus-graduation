package ru.yandex.practicum.graduation.core.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.graduation.core.event.model.LocationEntity;


public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
}
