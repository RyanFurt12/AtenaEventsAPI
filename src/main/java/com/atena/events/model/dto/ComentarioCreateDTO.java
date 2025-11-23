package com.atena.events.model.dto;

import lombok.Data;

@Data
public class ComentarioCreateDTO {
    private Long eventoId;
    private Long usuarioId;
    private String texto;
}

