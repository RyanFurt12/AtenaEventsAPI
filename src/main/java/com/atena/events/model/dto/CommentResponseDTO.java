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
    private String authorAvatarBase64;
    private String authorAvatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponseDTO(Comment c) {
        this.id = c.getId();
        this.text = c.getText();
        this.authorId = c.getAuthor().getId();
        this.authorName = c.getAuthor().getName();
        this.authorAvatarBase64 = c.getAuthor().getAvatarBase64();
        this.authorAvatarUrl = c.getAuthor().getAvatarUrl();
        this.createdAt = c.getCreatedAt();
        this.updatedAt = c.getUpdatedAt();
    }
}

