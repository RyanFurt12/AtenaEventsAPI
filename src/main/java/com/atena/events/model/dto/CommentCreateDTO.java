package com.atena.events.model.dto;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private Long eventId;
    private String text;
}
