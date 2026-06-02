package com.atena.events.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class WhiteboardDTO {
    private boolean activated;   // já foi ativado alguma vez
    private boolean active;      // ativado e dentro da janela de 1h
    private boolean expired;     // ativado mas a janela de 1h já passou (read-only)
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime serverNow;  // relógio do servidor no momento da resposta
    private long myPostItCount;  // quantos post-its o usuário atual já colou (0 se anônimo)
    private List<PostItResponseDTO> postIts;
}
