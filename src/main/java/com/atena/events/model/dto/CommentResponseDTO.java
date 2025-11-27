package com.atena.events.model.dto;

import java.time.LocalDateTime;

import com.atena.events.model.Comment;

import lombok.Data;

@Data
public class CommentResponseDTO {
    private Long id;
    private String text;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;

    public CommentResponseDTO(Comment c) {
        this.id = c.getId();
        this.text = c.getText();
        this.authorId = c.getAuthor().getId();
        this.authorName = c.getAuthor().getName();
        this.createdAt = c.getCreatedAt();
    }
}

