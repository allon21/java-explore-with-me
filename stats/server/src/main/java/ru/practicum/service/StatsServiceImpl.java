package ru.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.StatsDto;
import ru.practicum.StatsHitDto;
import ru.practicum.exception.StatsServiceException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.StatsHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public StatsHitDto saveHit(StatsHitDto statsHitDto) {
        log.info("Сохранение hit: {}", statsHitDto);
        try {
            StatsHit statsHit = StatsMapper.INSTANCE.statsHitFromDto(statsHitDto);
            StatsHit savedHit = repository.save(statsHit);
            StatsHitDto result = StatsMapper.INSTANCE.statsHitToDto(savedHit);
            log.info("Hit успешно сохранен: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Ошибка при сохранении hit: {}", e.getMessage(), e);
            throw new StatsServiceException("Не удалось сохранить hit в базу данных", e);
        }
    }

    @Override
    public List<StatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("Получение статистики: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        try {
            LocalDateTime startDate = LocalDateTime.parse(start, FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(end, FORMATTER);
            log.info("Проверка диапазона дат: startDate={}, endDate={}", startDate, endDate);
            if (endDate.isBefore(startDate)) {
                log.error("Ошибка валидации: Дата начала {} позже даты окончания {}", startDate, endDate);
                throw new ValidationException("Дата начала должна быть раньше даты окончания");
            }

            if (unique) {
                return repository.getUniqueHits(startDate, endDate, uris);
            } else {
                return repository.getHits(startDate, endDate, uris);
            }

        } catch (DateTimeParseException e) {
            log.error("Ошибка при парсинге даты: {}", e.getMessage(), e);
            throw new ValidationException("Неверный формат даты. Ожидается формат: yyyy-MM-dd HH:mm:ss");
        } catch (ValidationException e) {
            log.error("Ошибка при проверки даты: {}", e.getMessage(), e);
            throw new ValidationException("Неверный запрос с неверными датами");
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage(), e);
            throw new StatsServiceException("Ошибка при выполнении операции получения статистики", e);
        }
    }
}
