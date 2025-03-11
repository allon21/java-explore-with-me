package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StatisticsHandler {
    private final StatsClient statsClient;

    @Autowired
    public StatisticsHandler(StatsClient statisticsClient) {
        this.statsClient = statisticsClient;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getViews(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            log.debug("Список идентификаторов событий пуст. Пропуск запроса статистики.");
            return Collections.emptyMap();
        }
        log.debug("Подключение к серверу статистики. Запрос просмотров для событий с идентификаторами: {}", eventIds);
        try {
            Map<Long, Long> views = getViewsMap(eventIds);
            log.debug("Получены просмотры для {} событий", views.size());
            return views;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Не удалось получить данные о просмотрах с сервера статистики: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Transactional(readOnly = true)
    public Long extractViews(Map<Long, Long> viewsMap, Long eventId) {
        return (viewsMap.getOrDefault(eventId, 0L));
    }

    @Transactional
    public void saveHit(StatsHitDto statsHitDto) {
        log.debug("Подключение к серверу статистики. Сохранение информации о запросе: приложение={}, uri={}, ip={}",
                statsHitDto.getApp(),
                statsHitDto.getUri(),
                statsHitDto.getIp());
        try {
            statsClient.postHit(statsHitDto);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Не удалось сохранить информацию о запросе на сервере статистики: {}", e.getMessage());
        }
    }

    private Map<Long, Long> getViewsMap(List<Long> eventIds) {
        log.debug("Запрос статистики просмотров для событий: {}", eventIds);
        try {
            List<String> uris = buildUris(eventIds);

            ResponseEntity<Object> response = statsClient.getStats(
                    LocalDateTime.now().minusYears(1),
                    LocalDateTime.now().plusYears(1),
                    uris,
                    true
            );
            log.debug("Ответ от сервера статистики: Статус={}, Тело={}", response.getStatusCode(), response.getBody());

            List<Map<String, Object>> responseMap = (List<Map<String, Object>>) response.getBody();
            if (responseMap == null || responseMap.isEmpty()) {
                log.warn("Сервер статистики вернул пустой ответ.");
                return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
            }

            return responseMap.stream()
                    .filter(entry -> entry.containsKey("uri") && entry.containsKey("hits"))
                    .collect(Collectors.toMap(
                            entry -> extractEventIdFromUri(entry.get("uri").toString()),
                            entry -> Long.parseLong(entry.get("hits").toString())
                    ));
        } catch (Exception e) {
            log.error("Неожиданная ошибка при запросе статистики просмотров: {}", e.getMessage());
            return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }

    private List<String> buildUris(List<Long> ids) {
        return new ArrayList<>(ids.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList()));
    }

    private Long extractEventIdFromUri(String uri) {
        try {
            return Long.parseLong(uri.split("/")[2]);
        } catch (Exception e) {
            log.error("Не удалось извлечь идентификатор события из URI: {}", uri);
            throw new IllegalArgumentException("Неверный формат URI: " + uri);
        }
    }
}