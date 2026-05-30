package com.atena.events.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordDTO {

    @NotBlank(message = "Token é obrigatório.")
    private String token;

    @NotBlank(message = "A nova senha é obrigatória.")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Senha deve conter ao menos 8 caracteres, uma letra maiúscula e um número."
    )
    private String newPassword;
}
