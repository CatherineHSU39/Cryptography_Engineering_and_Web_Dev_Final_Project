package com.nycu.ce.ciphergame.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TwoFaRequest {

    @NotBlank(message = "2FA code is required")
    @Pattern(regexp = "\\d{6}", message = "2FA code must be exactly 6 digits")
    private String code;
}
