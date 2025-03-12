package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.StatisticsHandler;
import ru.practicum.StatsHitDto;
import ru.practicum.dto.event.*;
import ru.practicum.enums.EventActionStateAdmin;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.SortingOptions;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.mappers.EventUpdater;
import ru.practicum.mappers.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final StatisticsHandler statisticsHandler;

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));
    }

    private Event getEventByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.getByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));
    }

    private Event getPublishedEventById(Long eventId) {
        return eventRepository.getByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Опубликованное событие с id=" + eventId + " не найдено."));
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть не менее чем через 2 часа от текущего времени.");
        }
    }

    private void sendStatsRequest(HttpServletRequest request) {
        statisticsHandler.saveHit(StatsHitDto.builder()
                .app("service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        validateEventDate(newEventDto.getEventDate());
        User user = getUserById(userId);

        Event event = EventMapper.INSTANCE.getEvent(newEventDto);
        Location location = locationRepository.save(event.getLocation());
        event.setInitiator(user);
        event.setState(EventState.PENDING);
        event.setCreated(LocalDateTime.now());
        event.setLocation(location);
        event.setConfirmedRequests(0L);

        Event savedEvent = eventRepository.save(event);
        log.info("Событие с id={} успешно добавлено пользователем с id={}.", savedEvent.getId(), userId);
        return EventMapper.INSTANCE.getEventFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, int from, int size) {
        getUserById(userId);
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        log.debug("Запрос списка событий для пользователя с id={}. Пагинация: from={}, size={}", userId, from, size);
        return eventRepository.getAllByInitiatorId(userId, pageable)
                .stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        getUserById(userId);
        Event event = getEventByIdAndInitiatorId(eventId, userId);
        log.debug("Запрос события с id={} для пользователя с id={}.", eventId, userId);
        return EventMapper.INSTANCE.getEventFullDto(event);
    }

    @Override
    public EventFullDto getEventPublic(Long eventId, HttpServletRequest request) {
        Event event = getPublishedEventById(eventId);
        sendStatsRequest(request);

        Map<Long, Long> viewsMap = statisticsHandler.getViews(List.of(eventId));
        Long views = statisticsHandler.extractViews(viewsMap, eventId);
        event.setViews(views + 1);
        eventRepository.save(event);
        log.debug("Запрос публичного события с id={}. Просмотры: {}", eventId, views);
        return EventMapper.INSTANCE.getEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long eventId, Long userId, UpdateEventUserRequest updateEventUserRequest) {
        validateEventDate(updateEventUserRequest.getEventDate());
        getUserById(userId);
        Event event = getEventByIdAndInitiatorId(eventId, userId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя обновить опубликованное событие с id=" + eventId + ".");
        }

        EventUpdater.INSTANCE.update(updateEventUserRequest, event);
        log.info("Событие с id={} успешно обновлено пользователем с id={}.", eventId, userId);
        return EventMapper.INSTANCE.getEventFullDto(event);
    }

    @Override
    @Transactional
    public ParticipationRequestDto newParticipationRequest(Long reqUserId, Long eventId) {
        if (requestRepository.findByUserIdAndEventId(reqUserId, eventId).isPresent()) {
            throw new ConflictException("Запрос на участие уже существует для пользователя с id=" + reqUserId + " и события с id=" + eventId + ".");
        }

        User user = getUserById(reqUserId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        if ((event.getInitiator().getId()) == reqUserId) {
            throw new ConflictException("Инициатор события не может подавать запрос на участие в своем событии.");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Событие еще не опубликовано.");
        }

        long confirmedRequests = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L;
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequests) {
            throw new ConflictException("Достигнут лимит участников для события с id=" + eventId + ".");
        }

        Request request = new Request();
        request.setRequesterId(user);
        request.setEventId(event);
        request.setCreated(LocalDateTime.now());

        if (event.getParticipantLimit() != 0 && event.getRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(confirmedRequests + 1);
            eventRepository.save(event);
        }

        log.info("Создан новый запрос на участие для пользователя с id={} в событии с id={}.", reqUserId, eventId);
        return RequestMapper.INSTANCE.getParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден."));

        if (!((request.getRequesterId().getId()) == userId)) {
            throw new ConflictException("Пользователь с id=" + userId + " не является инициатором запроса с id=" + requestId + ".");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        log.info("Запрос на участие с id={} отменен пользователем с id={}.", requestId, userId);
        return RequestMapper.INSTANCE.getParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestsByUserId(Long userId) {
        getUserById(userId);
        log.debug("Запрос всех запросов на участие для пользователя с id={}.", userId);
        return requestRepository.findAllByUserId(userId)
                .stream()
                .map(RequestMapper.INSTANCE::getParticipationRequestDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getEventAllRequests(Long userId, Long eventId) {
        getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        if (!((event.getInitiator().getId()) == userId)) {
            throw new ConflictException("Пользователь с id=" + userId + " не является инициатором события с id=" + eventId + ".");
        }

        log.debug("Запрос всех запросов на участие для события с id={}.", eventId);
        return requestRepository.findAllByEventId(eventId)
                .stream()
                .map(RequestMapper.INSTANCE::getParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        validateEventDate(updateRequest.getEventDate());
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        if (updateRequest.getStateAction() != null) {
            EventState newState = mapStateActionToEventState(updateRequest.getStateAction());

            if (newState == EventState.PUBLISHED && event.getState() != EventState.PENDING) {
                throw new ConflictException("Нельзя опубликовать событие, которое не находится в состоянии PENDING.");
            }
            if (newState == EventState.CANCELED && event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Нельзя отменить опубликованное событие.");
            }

            event.setState(newState);
        }

        EventUpdater.INSTANCE.update(updateRequest, event);
        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с id={} успешно обновлено администратором.", eventId);
        return EventMapper.INSTANCE.getEventFullDto(updatedEvent);
    }

    private EventState mapStateActionToEventState(EventActionStateAdmin stateAction) {
        return switch (stateAction) {
            case PUBLISH_EVENT -> EventState.PUBLISHED;
            case REJECT_EVENT -> EventState.CANCELED;
        };
    }

    @Override
    public List<EventShortDto> getEventsByFilterPublic(String text,
                                                       List<Long> categories,
                                                       Boolean paid,
                                                       String rangeStart,
                                                       String rangeEnd,
                                                       Boolean onlyAvailable,
                                                       SortingOptions sortingOptions,
                                                       int from,
                                                       int size,
                                                       HttpServletRequest request) {

        if (categories == null || categories.isEmpty()) {
            categories = categoryRepository.findAll().stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
            log.debug("Категории не указаны. Используются все доступные категории: {}", categories);
        }

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : LocalDateTime.now();
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : LocalDateTime.of(2100, 1, 1, 0, 0, 0);

        if (end.isBefore(start)) {
            throw new ValidationException("Дата окончания не может быть раньше даты начала.");
        }

        Pageable pageable = sortingOptions == SortingOptions.EVENT_DATE
                ? PageRequest.of(from, size, Sort.by("eventDate").descending())
                : sortingOptions == SortingOptions.VIEWS
                ? PageRequest.of(from, size, Sort.by("views").descending())
                : PageRequest.of(from, size);

        log.debug("Попытка получить события. Параметры: текст={}, категории={}, платное={}, начало={}, конец={}, только доступные={}, сортировка={}. Пагинация: from={}, size={}",
                text, categories, paid, start, end, onlyAvailable, sortingOptions, from, size);

        List<Event> events = eventRepository.getAllByFilterPublic(text, categories, paid, start, end,
                onlyAvailable, EventState.PUBLISHED, pageable);

        if (events.isEmpty()) {
            log.debug("События по заданным фильтрам не найдены.");
            return List.of();
        }

        sendStatsRequest(request);
        eventRepository.saveAll(events);
        return events.stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Override
    public List<EventFullDto> getEventsByFilterAdmin(List<Long> users,
                                                     List<EventState> states,
                                                     List<Long> categories,
                                                     String rangeStart,
                                                     String rangeEnd,
                                                     int from,
                                                     int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : LocalDateTime.of(2100, 1, 1, 0, 0);

        if (states == null || states.isEmpty()) {
            states = List.of(EventState.PENDING, EventState.CANCELED, EventState.PUBLISHED);
            log.debug("Состояния не указаны. Используются все доступные состояния: {}", states);
        }

        if (users == null || users.isEmpty()) {
            users = userRepository.findAll().stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            log.debug("Пользователи не указаны. Используются все доступные пользователи: {}", users);
        }

        if (categories == null || categories.isEmpty()) {
            categories = categoryRepository.findAll().stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
            log.debug("Категории не указаны. Используются все доступные категории: {}", categories);
        }

        return eventRepository.getEventsByFilterAdmin(users, states, categories, start, end, pageable)
                .stream()
                .map(EventMapper.INSTANCE::getEventFullDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        if (!((event.getInitiator().getId()) == userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не является инициатором события с id=" + eventId + ".");
        }

        Collection<Request> requests = requestRepository.findAllRequestsOnEventByIds(eventId, updateRequest.getRequestIds());
        long confirmedRequests = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L;
        int limit = (event.getParticipantLimit() - (int) confirmedRequests);
        int confirmed = (int) confirmedRequests;

        if (limit <= 0) {
            throw new ConflictException("Достигнут лимит на участие в событии с id=" + eventId + ".");
        }

        for (Request req : requests) {
            if (!req.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Статус запроса с id=" + req.getId() + " не является PENDING.");
            }
            if (updateRequest.getStatus().equals(RequestStatus.REJECTED)) {
                req.setStatus(RequestStatus.REJECTED);
            } else if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                req.setStatus(RequestStatus.CONFIRMED);
                confirmed++;
            } else if (limit == 0) {
                req.setStatus(RequestStatus.REJECTED);
            } else {
                req.setStatus(RequestStatus.CONFIRMED);
                limit--;
            }
            requestRepository.save(req);
        }

        if (event.getParticipantLimit() != 0) {
            event.setConfirmedRequests((long) event.getParticipantLimit() - limit);
        } else {
            event.setConfirmedRequests((long) confirmed);
        }
        eventRepository.save(event);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(requestRepository.findAllRequestsOnEventByIdsAndStatus(eventId,
                        updateRequest.getRequestIds(),
                        RequestStatus.CONFIRMED)
                .stream()
                .map(RequestMapper.INSTANCE::getParticipationRequestDto)
                .toList());
        result.setRejectedRequests(requestRepository.findAllRequestsOnEventByIdsAndStatus(eventId,
                        updateRequest.getRequestIds(),
                        RequestStatus.REJECTED)
                .stream()
                .map(RequestMapper.INSTANCE::getParticipationRequestDto)
                .toList());
        log.info("Статусы запросов на участие для события с id={} обновлены.", eventId);
        return result;
    }
}