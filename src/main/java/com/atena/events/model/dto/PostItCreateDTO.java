package com.atena.events.model.dto;

import com.atena.events.model.PostItType;

import lombok.Data;

@Data
public class PostItCreateDTO {
    private PostItType type;
    private String text;        // mensagem (TEXT) ou legenda 1-palavra (PHOTO)
    private String imageBase64; // apenas PHOTO
    private Double xPct;
    private Double yPct;
}
