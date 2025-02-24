package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.StatsDto;
import ru.practicum.StatsHitDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.StatsHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceImplTest {

    @Mock
    private StatsRepository repository;

    @InjectMocks
    private StatsServiceImpl statsService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String APP_NAME = "service";
    private static final String URI = "/events/1";
    private static final String IP = "192.163.0.1";
    private static final String START = "2023-09-06 11:11:11";
    private static final String END = "2023-09-07 12:12:12";
    private static final List<String> URIS = List.of(URI);

    private StatsHitDto statsHitDto;
    private StatsHit statsHit;

    @BeforeEach
    public void setUp() {
        statsHitDto = createStatsHitDto();
        statsHit = StatsMapper.INSTANCE.statsHitFromDto(statsHitDto);
    }

    private StatsHitDto createStatsHitDto() {
        StatsHitDto dto = new StatsHitDto();
        dto.setApp(APP_NAME);
        dto.setUri(URI);
        dto.setIp(IP);
        dto.setTimestamp(LocalDateTime.now());
        return dto;
    }

    private StatsDto createStatsDto(Long hits) {
        return new StatsDto(APP_NAME, URI, hits);
    }

    private LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, FORMATTER);
    }

    private void assertHitDtoEquals(StatsHitDto expected, StatsHitDto actual) {
        assertAll(
                () -> assertEquals(expected.getApp(), actual.getApp()),
                () -> assertEquals(expected.getUri(), actual.getUri()),
                () -> assertEquals(expected.getIp(), actual.getIp()),
                () -> assertEquals(expected.getTimestamp(), actual.getTimestamp())
        );
    }

    private void assertStatsDtoListEquals(List<StatsDto> expected, List<StatsDto> actual) {
        assertAll(
                () -> assertEquals(expected.size(), actual.size()),
                () -> assertEquals(expected.get(0), actual.get(0))
        );
    }

    @Test
    public void timestampIsPreservedTest() {
        LocalDateTime expectedTimestamp = LocalDateTime.now();
        statsHitDto.setTimestamp(expectedTimestamp);

        when(repository.save(any(StatsHit.class))).thenAnswer(invocation -> {
            StatsHit savedHit = invocation.getArgument(0);
            assertEquals(expectedTimestamp, savedHit.getTimestamp());
            return savedHit;
        });

        StatsHitDto result = statsService.saveHit(statsHitDto);

        assertNotNull(result);
        assertEquals(expectedTimestamp, result.getTimestamp());
        verify(repository, times(1)).save(any(StatsHit.class));
    }

    @Test
    public void saveHitTest() {
        when(repository.save(any(StatsHit.class))).thenReturn(statsHit);

        StatsHitDto result = statsService.saveHit(statsHitDto);

        assertHitDtoEquals(statsHitDto, result);
        verify(repository, times(1)).save(any(StatsHit.class));
    }

    @Test
    public void getStatsWithUniqueHitsTest() {
        LocalDateTime startDate = parseDateTime(START);
        LocalDateTime endDate = parseDateTime(END);

        List<StatsDto> expectedStats = List.of(createStatsDto(1L));
        when(repository.getUniqueHits(startDate, endDate, URIS)).thenReturn(expectedStats);

        List<StatsDto> result = statsService.getStats(START, END, URIS, true);

        assertStatsDtoListEquals(expectedStats, result);
        verify(repository, times(1)).getUniqueHits(startDate, endDate, URIS);
    }

    @Test
    public void getStatsWithNonUniqueHitsTest() {
        LocalDateTime startDate = parseDateTime(START);
        LocalDateTime endDate = parseDateTime(END);

        List<StatsDto> expectedStats = List.of(createStatsDto(2L));
        when(repository.getHits(startDate, endDate, URIS)).thenReturn(expectedStats);

        List<StatsDto> result = statsService.getStats(START, END, URIS, false);

        assertStatsDtoListEquals(expectedStats, result);
        verify(repository, times(1)).getHits(startDate, endDate, URIS);
    }

    @Test
    public void getStatsWithInvalidDateFormatTest() {
        String invalidStart = "2022-11-11T21:22:23";

        assertThrows(ValidationException.class, () -> statsService.getStats(invalidStart, END, URIS, true));
        verifyNoInteractions(repository);
    }

    @Test
    public void getStatsWithExactTimeRangeTest() {
        LocalDateTime startDate = parseDateTime(START);
        LocalDateTime endDate = parseDateTime(END);

        List<StatsDto> expectedStats = List.of(createStatsDto(1L));
        when(repository.getUniqueHits(startDate, endDate, URIS)).thenReturn(expectedStats);

        List<StatsDto> result = statsService.getStats(START, END, URIS, true);

        assertStatsDtoListEquals(expectedStats, result);
        verify(repository, times(1)).getUniqueHits(startDate, endDate, URIS);
    }
}