package com.atena.events.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GuestCreateDTO {

    @NotBlank(message = "Username é obrigatório.")
    @Size(min = 2, max = 30, message = "Username deve ter entre 2 e 30 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-]+$",
             message = "Username pode conter apenas letras, números, _ e -.")
    private String username;
}
