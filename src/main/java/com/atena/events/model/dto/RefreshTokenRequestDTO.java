package com.atena.events.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {
    @NotBlank(message = "O refresh token é obrigatório.")
    private String refreshToken;
}
