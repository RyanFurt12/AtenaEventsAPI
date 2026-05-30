package com.atena.events.model.dto;

import com.atena.events.model.Participation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipantSummaryDTO {
    private Long userId;
    private String name;
    private String email;
    private String avatarBase64;
    private String avatarUrl;
    private String accountType;
    private LocalDateTime joinedAt;

    public ParticipantSummaryDTO(Participation p) {
        var user = p.getUser();
        this.userId = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.avatarBase64 = user.getAvatarBase64();
        this.avatarUrl = user.getAvatarUrl();
        this.accountType = user.getAccountType().name();
        this.joinedAt = p.getCreatedAt();
    }
}
