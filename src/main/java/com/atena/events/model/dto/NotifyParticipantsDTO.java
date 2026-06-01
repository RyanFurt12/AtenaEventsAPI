package com.atena.events.model.dto;

import lombok.Data;

/**
 * Corpo do pedido do dono do evento para notificar os participantes por email.
 * {@code phase} = "PRE" (lembrete pré-evento) | "POST" (agradecimento pós-evento).
 * {@code customMessage} é opcional — texto extra do organizador anexado ao template.
 */
@Data
public class NotifyParticipantsDTO {
    private String phase;
    private String customMessage;
}
