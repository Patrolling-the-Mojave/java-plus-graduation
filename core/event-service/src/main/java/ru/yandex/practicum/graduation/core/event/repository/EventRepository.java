package ru.yandex.practicum.graduation.core.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.yandex.practicum.graduation.core.event.model.Event;


import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    Page<Event> findByInitiatorIdOrderByCreatedOnDesc(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    Optional<Event> findByIdAndState(Long id, Event.EventState eventState);

    Set<Event> findAllByIdIn(Set<Long> eventIds);
}
