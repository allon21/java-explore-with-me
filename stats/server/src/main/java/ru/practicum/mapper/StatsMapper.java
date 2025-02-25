package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.StatsHitDto;
import ru.practicum.model.StatsHit;

@Mapper
public interface StatsMapper {
    StatsMapper INSTANCE = Mappers.getMapper(StatsMapper.class);

    StatsHit statsHitFromDto(StatsHitDto statsHitDto);

    StatsHitDto statsHitToDto(StatsHit statsHit);
}
