package ru.yandex.practicum.graduation.core.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.graduation.core.dto.ConfirmedRequestsCountDto;
import ru.yandex.practicum.graduation.core.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Integer countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT NEW ru.yandex.practicum.graduation.core.dto.ConfirmedRequestsCountDto(" +
            "r.event.id, COUNT(r)) " +
            "FROM Request r " +
            "WHERE r.event.id IN :eventIds AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event.id")
    List<ConfirmedRequestsCountDto> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> requestIds);

    List<Request> findAllByIdInAndEventId(List<Long> ids, Long eventId);


    @Query("""
            SELECT r FROM Request r
            JOIN FETCH r.event
            WHERE r.requester.id = :requesterId
            """)
    List<Request> findAllByRequesterId(@Param("requesterId") Long requesterId);

    @Query("""
            SELECT COUNT(r) > 0 FROM Request r
            WHERE r.event.id = :eventId AND r.requester.id = :requesterId
            """)
    boolean existsByRequesterIdAndEventId(@Param("requesterId") Long requesterId, @Param("eventId") Long eventId);

    int countByEvent_Id(Long eventId);

}
