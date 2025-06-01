package com.nycu.ce.ciphergame.dek.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DekRequest {

    @NotNull(message = "ownerId must not be empty")
    private UUID ownerId;

    @NotBlank(message = "dek must not be empty")
    private String dek;
}
