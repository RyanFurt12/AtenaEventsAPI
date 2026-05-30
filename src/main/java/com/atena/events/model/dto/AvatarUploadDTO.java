package com.atena.events.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvatarUploadDTO {

    @NotBlank(message = "A imagem é obrigatória.")
    private String avatarBase64;
}
