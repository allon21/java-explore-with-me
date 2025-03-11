package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.enums.RequestStatus;
import ru.practicum.model.Request;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("""
            SELECT r
            FROM Request as r
            WHERE r.eventId.id = :eventId
            AND r.requesterId.id = :userId
            """)
    Optional<Request> findByUserIdAndEventId(long userId, long eventId);

    @Query("""
            SELECT r
            FROM Request as r
            WHERE r.requesterId.id = :userId
            """)
    Collection<Request> findAllByUserId(long userId);

    @Query("""
            SELECT r
            FROM Request as r
            WHERE r.eventId.id = :eventId
            """)
    Collection<Request> findAllByEventId(long eventId);

    @Query("""
            SELECT r
            FROM Request as r
            WHERE (r.eventId.id = :eventId)
            AND (r.status = :status)
            AND (r.id IN :requestIds)
            """)
    Collection<Request> findAllRequestsOnEventByIdsAndStatus(long eventId, List<Long> requestIds, RequestStatus status);

    @Query("""
            SELECT r
            FROM Request as r
            WHERE (r.eventId.id = :eventId)
            AND (r.id IN :requestIds)
            """)
    Collection<Request> findAllRequestsOnEventByIds(long eventId, List<Long> requestIds);
}
