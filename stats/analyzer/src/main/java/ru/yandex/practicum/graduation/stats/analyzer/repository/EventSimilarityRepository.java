package ru.yandex.practicum.graduation.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.graduation.stats.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    @Query("SELECT es FROM EventSimilarity es WHERE es.eventA = :eventId ORDER BY es.similarityScore DESC")
    List<EventSimilarity> findByEventA(@Param("eventId") Long eventId);

    @Query("SELECT es FROM EventSimilarity es WHERE es.eventB = :eventId ORDER BY es.similarityScore DESC")
    List<EventSimilarity> findByEventB(@Param("eventId") Long eventId);

    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);

    void deleteByEventAOrEventB(Long eventA, Long eventB);
}
