package com.atena.events.model.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentResponseDTO {
    private String text;
    private String authorName;
    private LocalDateTime createdAt;

    public CommentResponseDTO(String text, String userName, LocalDateTime createdAt) {
        this.text = text;
        this.authorName = userName;
        this.createdAt = createdAt;
    }
}

