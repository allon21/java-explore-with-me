package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortingOptions;

import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByUserId(Long userId, int from, int size);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto getEventPublic(Long eventId, HttpServletRequest request);

    EventFullDto updateEventByUser(Long eventId, Long userId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    List<ParticipationRequestDto> getEventAllRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatuses(Long userId,
                                                         Long eventId,
                                                         EventRequestStatusUpdateRequest updateRequest);

    List<EventShortDto> getEventsByFilterPublic(String text,
                                                List<Long> categories,
                                                Boolean paid,
                                                String rangeStart,
                                                String rangeEnd,
                                                Boolean onlyAvailable,
                                                SortingOptions sortingOptions,
                                                int from,
                                                int size,
                                                HttpServletRequest request);

    List<EventFullDto> getEventsByFilterAdmin(List<Long> users,
                                              List<EventState> states,
                                              List<Long> categories,
                                              String rangeStart,
                                              String rangeEnd,
                                              int from,
                                              int size);


    ParticipationRequestDto newParticipationRequest(Long reqUserId, Long eventId);

    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getAllRequestsByUserId(Long userId);
}
