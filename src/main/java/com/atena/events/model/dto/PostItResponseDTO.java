package com.atena.events.model.dto;

import java.time.LocalDateTime;

import com.atena.events.model.PostIt;
import com.atena.events.model.PostItType;

import lombok.Data;

@Data
public class PostItResponseDTO {
    private Long id;
    private PostItType type;
    private String text;
    private String imageBase64;
    private String color;
    private Double xPct;
    private Double yPct;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private LocalDateTime createdAt;

    public PostItResponseDTO(PostIt p) {
        this.id = p.getId();
        this.type = p.getType();
        this.text = p.getText();
        this.imageBase64 = p.getImageBase64();
        this.color = p.getColor();
        this.xPct = p.getXPct();
        this.yPct = p.getYPct();
        this.authorId = p.getAuthor().getId();
        this.authorName = p.getAuthor().getName();
        this.authorAvatarUrl = p.getAuthor().getAvatarUrl();
        this.createdAt = p.getCreatedAt();
    }
}
