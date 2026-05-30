package com.atena.events.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailChangeRequestDTO {

    @NotBlank(message = "A senha atual é obrigatória.")
    private String currentPassword;

    @NotBlank(message = "O novo email é obrigatório.")
    @Email(message = "Email inválido.")
    private String newEmail;
}
