package com.atena.events.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @NotBlank(message = "O nome é obrigatório.")
    private String name;

    // O email NÃO é editável por aqui — a troca de email é um fluxo próprio
    // (senha atual + confirmação por link). Ver /users/{id}/email.
}
