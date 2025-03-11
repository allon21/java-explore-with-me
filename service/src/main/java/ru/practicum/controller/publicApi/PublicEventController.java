package ru.practicum.controller.publicApi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.enums.SortingOptions;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(@RequestParam(required = false) String text,
                                                         @RequestParam(required = false) List<Long> categories,
                                                         @RequestParam(required = false) Boolean paid,
                                                         @RequestParam(required = false) String rangeStart,
                                                         @RequestParam(required = false) String rangeEnd,
                                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                         @RequestParam(required = false) SortingOptions sortingOptions,
                                                         @RequestParam(defaultValue = "0") int from,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         HttpServletRequest httpServletRequest) {
        List<EventShortDto> events = eventService.getEventsByFilterPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sortingOptions, from, size, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        EventFullDto eventFullDto = eventService.getEventPublic(id, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(eventFullDto);
    }

}
