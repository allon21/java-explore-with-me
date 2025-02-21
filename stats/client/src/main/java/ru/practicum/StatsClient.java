package ru.practicum;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StatsClient extends BaseClient {
    @Value("${stats.url}")
    private String baseUri;

    public StatsClient(String baseUri, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(baseUri))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> postHit(StatsHitDto statsHitDto) {
        try {
            return post("/hit", null, statsHitDto);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Ошибка отправки hit на сервер статистики");
        }
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            return get("/stats", Map.of(
                    "start", start,
                    "end", end,
                    "unique", unique,
                    "uris", uris
            ));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Ошибка получения статистики с сервера");
        }
    }

}
