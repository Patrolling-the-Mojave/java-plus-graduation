package ru.yandex.practicum.graduation.stats.analyzer.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.graduation.stats.analyzer.model.UserInteraction;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    List<UserInteraction> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @Query("SELECT ui FROM UserInteraction ui " +
            "WHERE ui.userId = :userId " +
            "ORDER BY ui.updatedAt DESC")
    List<UserInteraction> findTopByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT ui.maxWeight FROM UserInteraction ui " +
            "WHERE ui.userId = :userId AND ui.eventId = :eventId")
    Optional<Double> findWeightByUserAndEvent(@Param("userId") Long userId,
                                              @Param("eventId") Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    List<UserInteraction> findAllByUserId(Long userId);

    List<UserInteraction> findAllByEventIdIn(List<Long> eventId);
}
