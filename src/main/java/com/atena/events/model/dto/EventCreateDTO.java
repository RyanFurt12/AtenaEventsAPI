package com.atena.events.model.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventCreateDTO {

    @NotBlank(message = "O título é obrigatório.")
    private String title;

    @NotBlank(message = "A descrição é obrigatória.")
    private String description;

    @NotBlank(message = "O tipo/categoria é obrigatório.")
    private String type;

    @NotNull(message = "A data é obrigatória.")
    private LocalDateTime date;

    private String imageBase64;
}