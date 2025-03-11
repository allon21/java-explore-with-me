package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.enums.EventState;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            SELECT e FROM Event e
            WHERE (:text IS NULL OR (e.annotation ILIKE %:text% OR e.description ILIKE %:text%))
            AND (:categoryIds IS NULL OR e.category.id IN :categoryIds)
            AND (:paid IS NULL OR e.paid = :paid)
            AND (e.eventDate >= :start)
            AND (e.state = :state)
            AND (:onlyAvailable = false OR e.participantLimit = 0 OR e.confirmedRequests <= e.participantLimit)
            AND (CAST(:end AS DATE) IS NULL OR e.eventDate <= :end)
            """)
    List<Event> getAllByFilterPublic(@Param("text") String text,
                                     @Param("categoryIds") List<Long> categoryIds,
                                     @Param("paid") Boolean paid,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("onlyAvailable") Boolean onlyAvailable,
                                     @Param("state") EventState state,
                                     Pageable pageable);

    @Query("""
            SELECT e
            FROM Event e
            WHERE (:userIds IS NULL OR e.initiator.id IN :userIds)
            AND (:states IS NULL OR e.state IN :states)
            AND (:categoryIds IS NULL OR e.category.id IN :categoryIds)
            AND (CAST(:start AS DATE) IS NULL OR e.eventDate >= :start)
            AND (CAST(:end AS DATE) IS NULL OR e.eventDate <= :end)
            ORDER BY e.eventDate DESC
            """)
    List<Event> getEventsByFilterAdmin(@Param("userIds") List<Long> userIds,
                                       @Param("states") List<EventState> states,
                                       @Param("categoryIds") List<Long> categoryIds,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       Pageable pageable);


    @Query("""
            SELECT e FROM Event e
            WHERE e.id = :eventId
            AND e.initiator.id = :userId
            """)
    Optional<Event> getByIdAndInitiatorId(@Param("eventId") Long eventId,
                                          @Param("userId") Long userId);

    @Query("""
            SELECT e
            FROM Event as e
            WHERE e.initiator.id = :userId
            ORDER BY e.eventDate DESC
            """)
    List<Event> getAllByInitiatorId(@Param("userId") Long userId,
                                    Pageable pageable);

    @Query("""
            SELECT e
            FROM Event e
            WHERE e.id = :eventId
            AND e.state = :state
            """)
    Optional<Event> getByIdAndState(@Param("eventId") Long eventId,
                                    @Param("state") EventState state);

}
