package com.atena.events.model.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EventDTO {
    private String title;
    private String description;
    private String type;
    private LocalDateTime date;
}