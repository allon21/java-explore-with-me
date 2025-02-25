package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.StatsDto;
import ru.practicum.model.StatsHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<StatsHit, Long> {
    @Query("SELECT new ru.practicum.StatsDto(sh.app, sh.uri, COUNT(sh.ip)) " +
            "FROM StatsHit AS sh " +
            "WHERE sh.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR sh.uri IN (:uris)) " +
            "GROUP BY sh.app, sh.uri " +
            "ORDER BY COUNT(sh.ip) DESC")
    List<StatsDto> getHits(@Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end,
                           @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.StatsDto(sh.app, sh.uri, COUNT(DISTINCT sh.ip)) " +
            "FROM StatsHit AS sh " +
            "WHERE sh.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR sh.uri IN (:uris)) " +
            "GROUP BY sh.app, sh.uri " +
            "ORDER BY COUNT(DISTINCT sh.ip) DESC")
    List<StatsDto> getUniqueHits(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("uris") List<String> uris);
}
