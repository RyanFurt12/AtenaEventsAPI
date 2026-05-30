package com.atena.events.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MergeGuestDTO {
    @NotNull
    private Long guestId;
}
