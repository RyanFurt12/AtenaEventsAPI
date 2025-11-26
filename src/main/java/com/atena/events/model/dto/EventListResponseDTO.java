package com.atena.events.model.dto;

import java.time.LocalDateTime;

import com.atena.events.model.Event;

import lombok.Data;

@Data
public class EventListResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String type;
    private LocalDateTime date;

    public EventListResponseDTO(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.type = event.getType();
        this.date = event.getDate();
    }
}