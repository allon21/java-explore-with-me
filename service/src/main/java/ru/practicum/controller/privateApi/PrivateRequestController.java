package ru.practicum.controller.privateApi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.ParticipationRequestDto;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class PrivateRequestController {
    private final EventService eventService;

    @GetMapping("/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        List<ParticipationRequestDto> requests = eventService.getAllRequestsByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(requests);
    }

    @PostMapping("/requests")
    public ResponseEntity<ParticipationRequestDto> addRequest(@PathVariable Long userId,
                                                              @RequestParam Long eventId) {
        ParticipationRequestDto request = eventService.newParticipationRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(@PathVariable Long userId,
                                                                 @PathVariable Long requestId) {
        ParticipationRequestDto request = eventService.cancelParticipationRequest(userId, requestId);
        return ResponseEntity.status(HttpStatus.OK).body(request);
    }

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(@PathVariable Long userId,
                                                                          @PathVariable Long eventId) {
        List<ParticipationRequestDto> requests = eventService.getEventAllRequests(userId, eventId);
        return ResponseEntity.status(HttpStatus.OK).body(requests);
    }

    @PatchMapping("/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequestStatuses(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        EventRequestStatusUpdateResult result = eventService.updateRequestStatuses(userId, eventId, updateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
