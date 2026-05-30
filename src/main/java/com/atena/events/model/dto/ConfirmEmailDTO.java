package com.atena.events.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmEmailDTO {

    @NotBlank(message = "Token é obrigatório.")
    private String token;
}
