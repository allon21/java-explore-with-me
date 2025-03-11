package ru.practicum.controller.privateApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventFullDto> addEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        EventFullDto eventFullDto = eventService.addEvent(userId, newEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventFullDto);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        EventFullDto eventFullDto = eventService.updateEventByUser(eventId, userId, updateEventUserRequest);
        return ResponseEntity.status(HttpStatus.OK).body(eventFullDto);
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(@PathVariable Long userId,
                                                         @RequestParam(defaultValue = "0") int from,
                                                         @RequestParam(defaultValue = "10") int size) {
        List<EventShortDto> events = eventService.getEventsByUserId(userId, from, size);
        return ResponseEntity.status(HttpStatus.OK).body(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        EventFullDto eventFullDto = eventService.getEventById(userId, eventId);
        return ResponseEntity.status(HttpStatus.OK).body(eventFullDto);
    }
}
