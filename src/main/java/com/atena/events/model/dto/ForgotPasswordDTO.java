package com.atena.events.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDTO {

    @NotBlank(message = "O email é obrigatório.")
    @Email(message = "Email inválido.")
    private String email;
}
