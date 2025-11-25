package com.atena.events.model.dto;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private Long eventId;
    private Long userId;
    private String text;
}

