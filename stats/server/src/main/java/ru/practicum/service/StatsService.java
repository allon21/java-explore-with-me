package ru.practicum.service;

import ru.practicum.StatsDto;
import ru.practicum.StatsHitDto;

import java.util.List;

public interface StatsService {
    StatsHitDto saveHit(StatsHitDto statsHitDto);

    List<StatsDto> getStats(String start, String end, List<String> uris, Boolean unique);
}
