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
    private String ownerAvatarBase64;
    private String ownerAvatarUrl;
    private String imageBase64;
    private List<String> participantsIds;
    private LocalDateTime preEventNotifiedAt;
    private LocalDateTime postEventNotifiedAt;

    public EventDTO(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.type = event.getType();
        this.date = event.getDate();
        this.ownerId = event.getOwner().getId();
        this.ownerName = event.getOwner().getName();
        this.ownerAvatarBase64 = event.getOwner().getAvatarBase64();
        this.ownerAvatarUrl = event.getOwner().getAvatarUrl();
        this.imageBase64 = event.getImageBase64();
        this.participantsIds = (event.getParticipations() == null)
        ? new ArrayList<>()
        : event.getParticipations().stream()
            .filter(p -> p != null && p.getUser().getId() != null && "OK".equals(p.getStatus()))
            .map(p -> p.getUser().getId().toString())
            .collect(Collectors.toList());
        this.preEventNotifiedAt = event.getPreEventNotifiedAt();
        this.postEventNotifiedAt = event.getPostEventNotifiedAt();
    }
}