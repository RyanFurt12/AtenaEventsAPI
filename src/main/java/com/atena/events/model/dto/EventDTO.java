package com.atena.events.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.atena.events.model.Event;

import lombok.Data;

@Data
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private String type;
    private LocalDateTime date;
    private Long ownerId;
    private String ownerName;
    private List<String> participantsIds;

    public EventDTO(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.type = event.getType();
        this.date = event.getDate();
        this.ownerId = event.getOwner().getId();
        this.ownerName = event.getOwner().getName();
        this.participantsIds = (event.getParticipants() == null)
        ? new ArrayList<>()
        : event.getParticipants().stream()
            .filter(p -> p != null && p.getId() != null)
            .map(p -> p.getId().toString())
            .collect(Collectors.toList());
    }
}