package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.enums.RequestStatus;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requesterId;
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event eventId;

    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
