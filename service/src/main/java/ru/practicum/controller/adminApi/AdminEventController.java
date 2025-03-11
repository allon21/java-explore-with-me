package ru.practicum.controller.adminApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.enums.EventState;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getEvents(@RequestParam(required = false) List<Long> users,
                                                        @RequestParam(required = false) List<EventState> states,
                                                        @RequestParam(required = false) List<Long> categories,
                                                        @RequestParam(required = false) String rangeStart,
                                                        @RequestParam(required = false) String rangeEnd,
                                                        @RequestParam(defaultValue = "0") int from,
                                                        @RequestParam(defaultValue = "10") int size) {
        List<EventFullDto> events = eventService.getEventsByFilterAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
        return ResponseEntity.status(HttpStatus.OK).body(events);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventAdminRequest updateRequest) {
        EventFullDto eventFullDto = eventService.updateEventByAdmin(eventId, updateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(eventFullDto);
    }
}
