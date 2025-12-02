package com.atena.events.model.dto;

import com.atena.events.model.Participation;

import lombok.Data;

@Data
public class ParticipateDTO {
    private Long eventId;
    private Long userId;


    public ParticipateDTO(Long eventId, Long userId) {
        this.eventId = eventId;
        this.userId = userId;
    }

    public ParticipateDTO(Participation p) {
        this.eventId = p.getEvent().getId();
        this.userId = p.getUser().getId();
    }
}

