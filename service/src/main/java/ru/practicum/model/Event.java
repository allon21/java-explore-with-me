package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "confirmed_requests")
    private Long confirmedRequests;

    @Column
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Category.class)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Location.class)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    private Boolean paid;
    @Column(name = "participant_limit")
    private Integer participantLimit;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @Column
    private String title;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;
    @Enumerated(EnumType.STRING)
    private EventState state;
    @Column
    private LocalDateTime created;
    @Column
    private LocalDateTime published;
    @Column
    private Long views;

}
