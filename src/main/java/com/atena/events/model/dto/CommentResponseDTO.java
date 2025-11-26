package com.atena.events.model.dto;

import java.time.LocalDateTime;

import com.atena.events.model.Comment;

import lombok.Data;

@Data
public class CommentResponseDTO {
    private String text;
    private String authorName;
    private LocalDateTime createdAt;

    public CommentResponseDTO(Comment c) {
        this.text = c.getText();
        this.authorName = c.getAuthor().getName();
        this.createdAt = c.getCreatedAt();
    }
}

