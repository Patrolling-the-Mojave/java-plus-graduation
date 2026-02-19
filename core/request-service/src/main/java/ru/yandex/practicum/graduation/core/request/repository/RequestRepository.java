package ru.yandex.practicum.graduation.core.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.graduation.core.dto.request.ConfirmedRequestsCountDto;
import ru.yandex.practicum.graduation.core.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("SELECT COUNT(r) FROM Request r WHERE r.eventId = :eventId AND r.status = 'CONFIRMED'")
    Integer countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT NEW ru.yandex.practicum.graduation.core.dto.request.ConfirmedRequestsCountDto(" +
            "r.eventId, COUNT(r)) " +
            "FROM Request r " +
            "WHERE r.eventId IN :eventIds AND r.status = 'CONFIRMED' " +
            "GROUP BY r.eventId")
    List<ConfirmedRequestsCountDto> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> requestIds);

    List<Request> findAllByIdInAndEventId(List<Long> ids, Long eventId);

    List<Request> findAllByRequesterId(@Param("requesterId") Long requesterId);

    @Query("""
            SELECT COUNT(r) > 0 FROM Request r
            WHERE r.eventId = :eventId AND r.requesterId = :requesterId
            """)
    boolean existsByRequesterIdAndEventId(@Param("requesterId") Long requesterId, @Param("eventId") Long eventId);

    int countByEventId(Long eventId);

}
