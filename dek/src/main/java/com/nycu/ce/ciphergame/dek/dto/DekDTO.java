package com.nycu.ce.ciphergame.dek.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DekDTO {

    @NotNull(message = "id must not be empty")
    private UUID id;

    @NotNull(message = "ownerId must not be empty")
    private UUID ownerId;

    @NotBlank(message = "nonce must not be empty")
    private String nonce;

    @NotBlank(message = "dek must not be empty")
    private String encrypted_dek;

    @NotBlank(message = "aad must not be empty")
    private String aad;
}
