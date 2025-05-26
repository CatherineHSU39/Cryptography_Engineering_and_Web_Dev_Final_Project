package com.nycu.ce.ciphergame.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "User name is required")
    private String username;
}
